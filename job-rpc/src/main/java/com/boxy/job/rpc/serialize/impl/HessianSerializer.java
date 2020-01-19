package com.boxy.job.rpc.serialize.impl;

import com.boxy.job.rpc.util.RpcException;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.boxy.job.rpc.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer extends Serializer {
	@Override
	public <T> byte[] serialize(T obj){
		try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			Hessian2Output ho = new Hessian2Output(os);

			ho.writeObject(obj);
			ho.flush();

			byte[] result = os.toByteArray();
			ho.close();
			return result;
		} catch (IOException e) {
			throw new RpcException(e);
		}
	}
	@Override
	public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
		try(ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
			Hessian2Input hi = new Hessian2Input(is);

			Object result = hi.readObject();
			hi.close();

			return result;
		} catch (IOException e) {
			throw new RpcException(e);
		}
	}
}
