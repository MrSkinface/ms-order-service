package ua.mike.micro.orderservice.models;

public enum OrderEvent {

    VALIDATE, VALIDATION_SUCCESS, VALIDATION_ERROR,
    ALLOCATE, ALLOCATION_SUCCESS, ALLOCATION_NO_INVENTORY, ALLOCATION_ERROR,
    PICK_UP, CANCEL
}