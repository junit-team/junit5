package org.junit.lambda.proposal02;

public class Person {
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCountry() {
        return country;
    }

    private final String firstName;
    private final String lastName;
    private final String country;

    public Person(String firstName, String lastName, String country) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
    }
}
