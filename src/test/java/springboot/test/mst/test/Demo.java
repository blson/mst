package springboot.test.mst.test;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.TimeOutDuration;
import io.appium.java_client.remote.MobileCapabilityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.PageFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import springboot.test.mst.base.BaseCase;
import springboot.test.mst.po.LoginPage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


@SpringBootTest(classes=Demo.class)
public class Demo extends BaseCase {

	private final Logger log = LogManager.getLogger(this.getClass());
	private LoginPage loginPage = new LoginPage();

	@BeforeClass
	private void BeforeClass() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME,platform);
		capabilities.setCapability("appPackage", appPackage);
		capabilities.setCapability("deviceName", deviceName);
		capabilities.setCapability("appActivity", appActivity);
		capabilities.setCapability("noReset", true);
		capabilities.setCapability("unicodeKeyboard", true);
		capabilities.setCapability("resetKeyboard", true);
		try {
			driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
		} catch (MalformedURLException e) {
			throw new RuntimeException("启动http://127.0.0.1:4723/wd/hub失败");
		}
		PageFactory.initElements(new AppiumFieldDecorator(driver,new TimeOutDuration(5, TimeUnit.SECONDS)),loginPage);
	}
	
	
	@Test(description="Android-UI自动化demo")
	public void demoTest() throws Exception{
		log.debug("Start Test...");
		String str = loginPage.videoTab.getText();
		if(str!=null){
			Assert.assertEquals("视频",str);
		}
		Assert.assertTrue(loginPage.myTab.isDisplayed(),"检查我的tab显示");
		log.info("End Test...");
	}
	
	
	@AfterClass
	private void AfterClass() {
		log.info("driver quit ....");
		driver.quit();
	}
	
	
}
