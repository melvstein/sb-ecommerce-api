package com.melvstein.ecommerce.api.domain.customer.Mapper;

import com.melvstein.ecommerce.api.domain.customer.document.Address;
import com.melvstein.ecommerce.api.domain.customer.dto.AddressDto;

public class AddressMapper {

    public static AddressDto toDto(Address address) {
        if (address == null) {
            return null;
        }

        return AddressDto.builder()
                .addressType(address.getAddressType())
                .street(address.getStreet())
                .district(address.getDistrict())
                .city(address.getCity())
                .province(address.getProvince())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .build();
    }

    public static Address toDocument(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        return Address.builder()
                .addressType(addressDto.addressType())
                .street(addressDto.street())
                .district(addressDto.district())
                .city(addressDto.city())
                .province(addressDto.province())
                .country(addressDto.country())
                .zipCode(addressDto.zipCode())
                .build();

    }
}
