package com.crdev.connect_rural_api.app.resident.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentResponseDto {
private  String key;
private  String communityKey;
private  String firstName;
private  String lastName;
private  String address;
private  Date   joinedAt;
private  Boolean active;
}
