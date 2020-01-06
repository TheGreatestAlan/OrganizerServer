package com;

import com.evernote.EvernoteApi;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class nguyenApplication extends Application<nguyenConfiguration> {

    public static void main(final String[] args) throws Exception {
        //new nguyenApplication().run(args);
        //String token = System.getenv("AUTH_TOKEN");
        String token = "S=s1:U=95a34:E=176c9f3bb22:C=16f72428e10:P=1cd:A=en-devtoken:V=2:H=5cff1bec49c8ecc113bf7afc5eacce33";
        EvernoteApi demo = new EvernoteApi(token);
        demo.GetTodoList();
    }

    @Override
    public String getName() {
        return "nguyen";
    }

    @Override
    public void initialize(final Bootstrap<nguyenConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final nguyenConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
