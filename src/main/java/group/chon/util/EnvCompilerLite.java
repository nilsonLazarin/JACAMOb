package group.chon.util;

import cartago.Workspace;
import javax.tools.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class EnvCompilerLite {
    static Logger logger = Logger.getLogger("CHON");
    static final String TARGET_PATH = "target"+System.getProperty("file.separator")+"classes";
    static volatile boolean ENV_CL_INSTALLED = false;

    public static void compileAndInstall(String path) {
        try {
            Path src = Paths.get(path);
            if (!Files.isDirectory(src)) return;

            List<File> sources;
            try (var s = Files.walk(src)) {
                sources = s.filter(p -> p.toString().endsWith(".java"))
                        .map(Path::toFile).collect(Collectors.toList());
            }
            if (sources.isEmpty()){
                return;
            }else{
                for (File source : sources) {
                    logger.log(Level.FINE,"Reading File: " +path+System.getProperty("file.separator")+source.getName());
                }
            }

            Path out = Paths.get(TARGET_PATH+System.getProperty("file.separator")+path);
            Files.createDirectories(out);

            JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
            if (jc == null){
                logger.log(Level.SEVERE, "No Java System Java Compiler");
                throw new IllegalStateException("Rode com um JDK (javac indisponível).");
            }else{
                logger.log(Level.FINE,"Compiling Java System Java Compiler");
            }

            try (var fm = jc.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
                fm.setLocation(StandardLocation.CLASS_OUTPUT, List.of(out.toFile()));
                var opts = List.of("-classpath", System.getProperty("java.class.path"));
                var units = fm.getJavaFileObjectsFromFiles(sources);
                boolean ok = jc.getTask(null, fm, null, opts, null, units).call();
                if (!ok) throw new IllegalStateException("Falha ao compilar fontes de src/env");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            URLClassLoader envCl = new URLClassLoader(new URL[]{ out.toUri().toURL() },
                    ClassLoader.getSystemClassLoader());
            Thread.currentThread().setContextClassLoader(envCl);
        } catch (Exception e) {
            throw new RuntimeException("Erro preparando ambiente: " + e.getMessage(), e);
        }
    }


    public static void ensureEnvClassLoaderInstalled(String sourceDir) {
        if (ENV_CL_INSTALLED) return;
        synchronized (Workspace.class) {
            if (ENV_CL_INSTALLED) return;
            try {
                compileAndInstall(sourceDir);
                var out = java.nio.file.Paths.get(TARGET_PATH+System.getProperty("file.separator")+sourceDir);
                if (java.nio.file.Files.isDirectory(out)) {
                    var url = out.toUri().toURL();
                    // parent = *o mesmo loader da app* (para ver JaCaMo/Cartago)
                    ClassLoader parent = ClassLoader.getSystemClassLoader();
                    var envCl = new java.net.URLClassLoader(new java.net.URL[]{url}, parent);
                    Thread.currentThread().setContextClassLoader(envCl);
                    ENV_CL_INSTALLED = true;
                } else {
                    logger.log(Level.SEVERE,TARGET_PATH+System.getProperty("file.separator")+sourceDir+" not exists.");
                }
            } catch (Exception e) {
                throw new RuntimeException("EnvClassLoader Fail", e);
            }
        }
    }

}
