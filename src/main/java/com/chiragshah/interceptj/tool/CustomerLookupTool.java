package com.chiragshah.interceptj.tool;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CustomerLookupTool
        implements EnterpriseTool<CustomerLookupArguments, CustomerRecord> {

    public static final String TOOL_NAME = "customer-lookup";

    private final Map<String, CustomerRecord> customers = Map.of(
            "CUST-1001",
            new CustomerRecord(
                    "CUST-1001",
                    "Demonstration Customer One",
                    "EAST",
                    "ACTIVE"),
            "CUST-2001",
            new CustomerRecord(
                    "CUST-2001",
                    "Demonstration Customer Two",
                    "WEST",
                    "ACTIVE"));

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public Class<CustomerLookupArguments> getArgumentType() {
        return CustomerLookupArguments.class;
    }

    @Override
    public CustomerRecord execute(CustomerLookupArguments arguments) {
        CustomerRecord customer = customers.get(arguments.customerId());

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Demonstration customer was not found.");
        }

        if (!customer.region().equals(arguments.requestedRegion())) {
            throw new IllegalArgumentException(
                    "The requested region does not match the customer record.");
        }

        return customer;
    }
}