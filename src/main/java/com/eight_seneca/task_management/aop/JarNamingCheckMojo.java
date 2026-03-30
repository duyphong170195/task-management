//package com.eight_seneca.task_management.aop;
//
//import org.apache.maven.plugin.AbstractMojo;
//import org.apache.maven.plugin.MojoExecutionException;
//import org.apache.maven.plugins.annotations.LifecyclePhase;
//import org.apache.maven.plugins.annotations.Mojo;
//import org.apache.maven.plugins.annotations.Parameter;
//import org.apache.maven.project.MavenProject;
//
//// Gắn mặc định vào phase validate để kiểm tra ngay từ đầu
//@Mojo(name = "check-naming", defaultPhase = LifecyclePhase.VALIDATE)
//public class JarNamingCheckMojo extends AbstractMojo {
//
//    // Inject thông tin của dự án đang sử dụng plugin này
//    @Parameter(defaultValue = "${project}", readonly = true, required = true)
//    private MavenProject project;
//
//    public void execute() throws MojoExecutionException {
//        String artifactId = project.getArtifactId();
//        String prefix = "abc-soft-";
//
//        getLog().info("Đang kiểm tra tiêu chuẩn đặt tên cho: " + artifactId);
//
//        if (!artifactId.startsWith(prefix)) {
//            // Ném ra ngoại lệ này sẽ làm Build Failure ngay lập tức
//            throw new MojoExecutionException(
//                "\n[QUY ĐỊNH CÔNG TY] ArtifactId '" + artifactId + "' không hợp lệ!\n" +
//                "Tên dự án phải bắt đầu bằng '" + prefix + "'. Vui lòng sửa lại trong pom.xml"
//            );
//        }
//
//        getLog().info("Tên dự án hợp lệ. Tiếp tục build...");
//    }
//}