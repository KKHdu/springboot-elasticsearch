package com.zhou.essearch.service;

public interface ServiceAspectDemo {
    public void addCustomer(String name,String password);
    public void deleteCustomer(String name);
    public String getCustomerById(int id);
    public void updateCustomer(int id,String name,String password);
}
