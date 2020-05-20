package com.boxy.job.rpc.remoting.provider;

import com.boxy.job.rpc.remoting.net.params.RpcResponse;
import com.boxy.job.rpc.util.IpUtil;
import com.boxy.job.rpc.util.NetUtil;
import com.boxy.job.rpc.util.ThrowableUtil;
import com.boxy.job.rpc.registry.Register;
import com.boxy.job.rpc.remoting.net.Server;
import com.boxy.job.rpc.remoting.net.impl.netty.server.NettyServer;
import com.boxy.job.rpc.remoting.net.params.BaseCallback;
import com.boxy.job.rpc.remoting.net.params.RpcRequest;
import com.boxy.job.rpc.serialize.Serializer;
import com.boxy.job.rpc.serialize.impl.HessianSerializer;
import com.boxy.job.rpc.util.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RpcProviderFactory {
	private static final Logger logger = LoggerFactory.getLogger(RpcProviderFactory.class);

	// ---------------------- config ----------------------
	private Class<? extends Server> server = NettyServer.class;
	private Class<? extends Serializer> serializer = HessianSerializer.class;

	private int corePoolSize = 60;
	private int maxPoolSize = 300;

	private String ip = null;					// for registry
	private int port = 7080;					// default port
	private String registryAddress;				// default use registryAddress to registry , otherwise use ip:port if registryAddress is null
	private String accessToken = null;

	private Class<? extends Register> serviceRegistry = null;
	private Map<String, String> serviceRegistryParam = null;

	// set
	public void setServer(Class<? extends Server> server) {
		this.server = server;
	}
	public void setSerializer(Class<? extends Serializer> serializer) {
		this.serializer = serializer;
	}
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setServiceRegistry(Class<? extends Register> serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
		this.serviceRegistryParam = serviceRegistryParam;
	}

	// get
	public Serializer getSerializerInstance() {
		return serializerInstance;
	}
	public int getPort() {
		return port;
	}
	public int getCorePoolSize() {
		return corePoolSize;
	}
	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	// ---------------------- start / stop ----------------------

	private Server serverInstance;
	private Serializer serializerInstance;
	private Register registerInstance;
	// private String serviceAddress;

	public void start() throws Exception {

		// valid
		if (this.server == null) {
			throw new RpcException("job-rpc provider server missing.");
		}
		if (this.serializer==null) {
			throw new RpcException("job-rpc provider serializer missing.");
		}
		if (!(this.corePoolSize>0 && this.maxPoolSize>0 && this.maxPoolSize>=this.corePoolSize)) {
			this.corePoolSize = 60;
			this.maxPoolSize = 300;
		}
		if (this.ip == null) {
			this.ip = IpUtil.getIp();
		}
		if (this.port <= 0) {
			this.port = 7080;
		}
		if (this.registryAddress==null || this.registryAddress.trim().length()==0) {
			this.registryAddress = IpUtil.getIpPort(this.ip, this.port);
		}
		if (NetUtil.isPortUsed(this.port)) {
			throw new RpcException("job-rpc provider port["+ this.port +"] is used.");
		}

		// init serializerInstance
		this.serializerInstance = serializer.newInstance();

		// start server
		// serviceAddress = IpUtil.getIpPort(this.ip, port);
		serverInstance = server.newInstance();
		serverInstance.setStartedCallback(new BaseCallback() {		// serviceRegistry started
			@Override
			public void run() throws Exception {
				// start registry
				if (serviceRegistry != null) {
					registerInstance = serviceRegistry.newInstance();
					registerInstance.start(serviceRegistryParam);
					if (serviceData.size() > 0) {
						registerInstance.registry(serviceData.keySet(), registryAddress);
					}
				}
			}
		});
		serverInstance.setStopedCallback(new BaseCallback() {		// serviceRegistry stoped
			@Override
			public void run() {
				// stop registry
				if (registerInstance != null) {
					if (serviceData.size() > 0) {
						registerInstance.remove(serviceData.keySet(), registryAddress);
					}
					registerInstance.stop();
					registerInstance = null;
				}
			}
		});
		serverInstance.start(this);
	}

	public void  stop() throws Exception {
		// stop server
		serverInstance.stop();
	}


	// ---------------------- server invoke ----------------------

	/**
	 * init local rpc service map
	 */
	private Map<String, Object> serviceData = new HashMap<String, Object>();
	public Map<String, Object> getServiceData() {
		return serviceData;
	}

	public static String makeServiceKey(String iface, String version){
		String serviceKey = iface;
		if (version!=null && version.trim().length()>0) {
			serviceKey += "#".concat(version);
		}
		return serviceKey;
	}

	public void addService(String iface, String version, Object serviceBean){
		String serviceKey = makeServiceKey(iface, version);
		serviceData.put(serviceKey, serviceBean);

		logger.info("job-rpc, provider factory add service success. serviceKey = {}, serviceBean = {}", serviceKey, serviceBean.getClass());
	}

	public RpcResponse invokeService(RpcRequest rpcRequest) {

		//  make response
		RpcResponse rpcResponse = new RpcResponse();
		rpcResponse.setRequestId(rpcRequest.getRequestId());

		// match service bean
		String serviceKey = makeServiceKey(rpcRequest.getClassName(), rpcRequest.getVersion());
		Object serviceBean = serviceData.get(serviceKey);

		// valid
		if (serviceBean == null) {
			rpcResponse.setErrorMsg("The serviceKey["+ serviceKey +"] not found.");
			return rpcResponse;
		}

		if (System.currentTimeMillis() - rpcRequest.getCreateMillisTime() > 3*60*1000) {
			rpcResponse.setErrorMsg("The timestamp difference between admin and executor exceeds the limit.");
			return rpcResponse;
		}
		if (accessToken!=null && accessToken.trim().length()>0 && !accessToken.trim().equals(rpcRequest.getAccessToken())) {
			rpcResponse.setErrorMsg("The access token[" + rpcRequest.getAccessToken() + "] is wrong.");
			return rpcResponse;
		}

		try {
			// invoke
			Class<?> serviceClass = serviceBean.getClass();
			String methodName = rpcRequest.getMethodName();
			Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
			Object[] parameters = rpcRequest.getParameters();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
			Object result = method.invoke(serviceBean, parameters);

			/*FastClass serviceFastClass = FastClass.create(serviceClass);
			FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
			Object result = serviceFastMethod.invoke(serviceBean, parameters);*/

			rpcResponse.setResult(result);
		} catch (Throwable t) {
			// catch error
			logger.error("job-rpc provider invokeService error.", t);
			rpcResponse.setErrorMsg(ThrowableUtil.toString(t));
		}

		return rpcResponse;
	}

}
