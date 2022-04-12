package com.aroygas.kivork;

import com.codeborne.selenide.Selenide;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Condition.appear;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;


public class KivorkTest {

    @Test (groups = {"web", "stale"})
    @Parameters({"url"})
    public void StaleElementTest (String url) {
        //There should be some reasonable limit for amount of tries to get stale element back
        int maxTries = 3;

        open(url);
        $(byAttribute("data-language", "java")).click(); //Clicking button to show text

        //This kind of code can be put in some wrapper
        for(int i=0; i<=maxTries; i++){
            try{
                WebElement element = $(byId("java_own_ip")).should(appear); // Waits until element appear
                Assert.assertTrue(element.getText().contains("String apikey = \"XXXXXXXXXXXXXX\";"));
                break;
            } catch(StaleElementReferenceException e){
                Selenide.refresh(); //Rebuilding DOM by refreshing the page
            }
        }
    }

    @Test (groups = {"api"})
    @Parameters({"url", "longitude", "latitude"})
    public void APICallTest (String url, float longitude, float latitude) {
        SoftAssert softAssert = new SoftAssert();
        Response response = given().contentType(JSON).get(url + "json");
        response.then().assertThat().statusCode(200); //a) Assert the response code;
        Map<String, Object> resultMap = response.jsonPath().getMap("$");//b) Parse the response;
        //c) Assert your latitude and longitude with a 0.01° tolerance
        softAssert.assertEquals(Float.parseFloat(resultMap.get("longitude").toString()),  longitude, 0.01);
        softAssert.assertEquals(Float.parseFloat(resultMap.get("latitude").toString()),  latitude, 0.01);
        softAssert.assertAll();
    }

    @Test (groups = {"web", "newtab"})
    @Parameters({"url"})
    public void SwitchTabsTest (String url) {
        Selenide.open(url);
        $(byText("Get Free Apikey")).click(); //Click on button that opens new tab
        //Collecting all tabs
        List<String> tabs = new ArrayList<String>(getWebDriver().getWindowHandles());
        Selenide.switchTo().window(tabs.get(1)); // Switching to new tab
        $("title").shouldHave(attribute("text", "freegeoip"));//Assert page title
        Selenide.switchTo().window(tabs.get(0)); // Switching to old tab
        $("title").shouldHave(attribute("text",
                "Free IP Geolocation API - FreeGeoIP.app"));//Assert page title
    }

    @Test (groups = {"web", "unload"})
    @Parameters({"url"})
    public void UnloadTest (String url) {
        int maxTries = 3;
        Selenide.open(url);
        WebElement element = getWebDriver().findElement(By.xpath("//a[@href = '/register']"));
        element.click(); //Click on button that opens new window

        //This kind of code can be put in some wrapper
        for(int i=0; i<=maxTries; i++) {
            try {
                element.getText();
                //If the page was unloaded old DOM should be destroyed StaleElementReferenceException should be thrown
            } catch (StaleElementReferenceException e) {
                break;
            }
        }
    }
}
