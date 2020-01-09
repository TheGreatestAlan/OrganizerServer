package com;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;
/*import javax.validation.constraints.*;
import javax.validation.constraints.NotEmpty;*/

public class OrganizerServerConfiguration extends Configuration {
    // TODO: implement service configuration
//    @NotEmpty
    private String template;

//    @NotEmpty
    private String defaultName = "Stranger";

//    @NotEmpty
    private String evernoteToken;

//    @NotEmpty
    private String evernoteConsumerKey;

//    @NotEmpty
    String evernoteConsumerSecret;

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

    @JsonProperty
    public String getEvernoteConsumerKey() {
        return evernoteConsumerKey;
    }

    @JsonProperty
    public void setEvernoteConsumerKey(String evernoteConsumerKey) {
        this.evernoteConsumerKey = evernoteConsumerKey;
    }

    @JsonProperty
    public String getEvernoteConsumerSecret() {
        return evernoteConsumerSecret;
    }

    @JsonProperty
    public void setEvernoteConsumerSecret(String evernoteConsumerSecret) {
        this.evernoteConsumerSecret = evernoteConsumerSecret;
    }

    @JsonProperty
    public String getEvernoteToken() {
        return evernoteToken;
    }

    @JsonProperty
    public void setEvernoteToken(String evernoteToken) {
        this.evernoteToken = evernoteToken;
    }
}
