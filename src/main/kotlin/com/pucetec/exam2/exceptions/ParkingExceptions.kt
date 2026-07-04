package com.pucetec.exam2.exceptions


open class ParkingException(message: String) : RuntimeException(message)

class ResourceNotFoundException(message: String) : ParkingException(message)

class EstacionamientoLlenoException(message: String) : ParkingException(message)

class BusinessValidationException(message: String) : ParkingException(message)