package com.aroygas.kivork;

import com.codeborne.selenide.Selenide;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;

public class KivorkTest {

    @Test
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
}
