package top.panson.core.handler;

import top.panson.core.config.BootstrapPropertiesInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;



/**
 *
 * @方法描述：该类的作用就是在程序启动时，在控制台打印hippo4j框架的启动图案的
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicThreadPoolBannerHandler implements InitializingBean {

    private final BootstrapPropertiesInterface properties;

    private final String DYNAMIC_THREAD_POOL = " :: Dynamic ThreadPool :: ";

    private final String HIPPO4J_GITHUB = "GitHub:  https://github.com/opengoofy/hippo4j";

    private final String HIPPO4J_SITE = "Site:    https://www.hippo4j.cn";

    private final int STRAP_LINE_SIZE = 50;


    //SpringBoot中InitializingBean的回调方法
    @Override
    public void afterPropertiesSet() {
        printBanner();
    }




    //在控制台打印程序启动图案的方法
    private void printBanner() {
        String banner = "  __     __                       ___ ___   __ \n" +
                " |  |--.|__|.-----..-----..-----.|   |   | |__|\n" +
                " |     ||  ||  _  ||  _  ||  _  ||   |   | |  |\n" +
                " |__|__||__||   __||   __||_____||____   | |  |\n" +
                "            |__|   |__|              |:  ||___|\n" +
                "                                     `---'     \n";
        if (properties.getBanner()) {
            String version = getVersion();
            version = (version != null) ? " (v" + version + ")" : "no version.";
            StringBuilder padding = new StringBuilder();
            while (padding.length() < STRAP_LINE_SIZE - (version.length() + DYNAMIC_THREAD_POOL.length())) {
                padding.append(" ");
            }
            System.out.println(AnsiOutput.toString(banner, AnsiColor.GREEN, DYNAMIC_THREAD_POOL, AnsiColor.DEFAULT,
                    padding.toString(), AnsiStyle.FAINT, version, "\n\n", HIPPO4J_GITHUB, "\n", HIPPO4J_SITE, "\n"));

        }
    }

    /**
     * Get version.
     *
     * @return hippo4j version
     */
    public static String getVersion() {
        final Package pkg = DynamicThreadPoolBannerHandler.class.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : "";
    }
}
