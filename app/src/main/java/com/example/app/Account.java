package com.example.app;

public class Account {
    private String accountName;
    private String username;
    private String password;
    private String documentId;

    public Account(String documentId,String accountName,String username,String password)
    {
        this.accountName=accountName;
        this.username=username;
        this.password=password;
        this.documentId=documentId;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getDocumentId(){return documentId;}
    public String getAccountName() {
        return accountName;
    }

}