package springboot.test.mst.base;

import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import springboot.test.mst.util.ExcelProvider;

import java.util.Iterator;


@TestPropertySource(locations={"classpath:properties/env.properties"})
public abstract class BaseCase extends  AbstractTestNGSpringContextTests{
	@Value("${ENV_NAME}")
	private String envParameter;
	public static AppiumDriver driver;
	public static WebDriver webDriver;
	@Value("${APPPACKAGE}")
	public String appPackage;

	@Value("${DEVICENAME}")
	public String deviceName;

	@Value("${APPACTIVITY}")
	public String appActivity;
	public String platform = "Android";
	private final Logger log = LogManager.getLogger(this.getClass());

	@BeforeSuite
	public void initialize() {

	}

	@BeforeTest
	public void beforeTest(){

	}


	@AfterTest
	public void afterTest(){
	}

	public String getEnvParameter() {
		return envParameter;
	}
	
	
	public Iterator<Object[]> excelProvider(Object obj, String methodName) {
		log.info("========  current env is  ========>{}",envParameter);
		return  new ExcelProvider(obj, methodName, envParameter);
	}
	
	
}
