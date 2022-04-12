package com.aroygas.kivork;

import com.codeborne.selenide.Selenide;
import io.restassured.response.Response;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.WebDriverRunner.*;
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
                $(byId("java_own_ip")).should(appear); // Waits until element appear, timeout can be set
                $(byId("java_own_ip")).shouldHave(text("String apikey = \"XXXXXXXXXXXXXX\";")); //Assert text
                break;
            }
            catch(StaleElementReferenceException e){
                //To my knowledge the only way to rebuild the DOM is refreshing the page
                Selenide.refresh(); //Refreshes the page
                System.out.println(e.getMessage());
            }
        }
    }

    @Test (groups = {"api"})
    @Parameters({"url", "longitude", "latitude"})
    public void APICallTest (String url, float longitude, float latitude) {
        Response response = given().
                contentType(JSON).
                get(url + "json");
        response.then().assertThat().statusCode(200); //a) Assert the response code;
        response.getBody().prettyPrint(); //b) Parse the response;
        //c) Assert your latitude and longitude with a 0.01° tolerance
        Assert.assertEquals(response.jsonPath().getFloat("longitude"),  longitude, 0.01);
        Assert.assertEquals(response.jsonPath().getFloat("latitude"),  latitude, 0.01);
    }

    @Test (groups = {"web", "newtab"})
    @Parameters({"url"})
    public void SwitchTabsTest (String url) {
        Selenide.open(url);
        $(byText("Get Free Apikey")).click(); //Click on button that opens new tab

        List<String> tabs = new ArrayList<String>(getWebDriver().getWindowHandles());
        Selenide.switchTo().window(tabs.get(1)); // Switching to new tab
        $("title").shouldHave(attribute("text", "freegeoip"));//Assert page title
        Selenide.switchTo().window(tabs.get(0)); // Switching to old tab
        $("title").shouldHave(attribute("text",
                "Free IP Geolocation API - FreeGeoIP.app"));//Assert page title
    }
}
