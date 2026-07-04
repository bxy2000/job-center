<!DOCTYPE html>
<html>
<head>
  	<#import "./common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
	<title>${I18n.admin_name}</title>
</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && cookieMap["job_adminlte_settings"]?exists && "off" == cookieMap["job_adminlte_settings"].value >sidebar-collapse</#if> ">
<div class="wrapper">
	<!-- header -->
	<@netCommon.commonHeader />
	<!-- left -->
	<@netCommon.commonLeft "help" />

	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>${I18n.job_help}</h1>
		</section>

		<!-- Main content -->
		<section class="content">
			<div class="callout callout-info">
				<h4>power shell 调用kettle转换</h4>
				<p>
				<pre>
c:
cd /
cd data-integration
./Pan.bat /file /kettle/教务学生表到学工学生表.ktr /level Detailed
				</pre>
				</p>
			</div>
			<div class="callout callout-info">
				<h4>power shell 调用kettle任务</h4>
				<p>
				<pre>
c:
cd /
cd data-integration
./Kitchen.bat /file /kettle/timer/timer.kjb /level Detailed
				</pre>
				</p>
			</div>
		</section>
		<!-- /.content -->
	</div>
	<!-- /.content-wrapper -->

	<!-- footer -->
	<@netCommon.commonFooter />
</div>
<@netCommon.commonScript />
</body>
</html>
