package com.mns;

import io.micronaut.serde.annotation.Serdeable;
@Serdeable
public record  Order(String id, String location){
}
