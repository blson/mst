package springboot.test.mst.po;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSFindBy;
import org.openqa.selenium.WebElement;

/*
 @author TangSan
 @DESCRIPTION 
 @create 2019-12-29
*/
public class LoginPage {

    @AndroidFindBy(xpath = "//*[@text='我的']")
    @iOSFindBy(xpath = "//*[@name='我的']")
    public WebElement myTab;

    @AndroidFindBy(xpath = "//*[@text='视频']")
    @iOSFindBy(xpath = "//*[@name='视频']")
    public WebElement videoTab;

}
