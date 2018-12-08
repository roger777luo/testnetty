package com.roger.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

public class testUserInfo {

    private static byte[] decode(UserInfoProto.Person p) {
        return p.toByteArray();
    }

    private static UserInfoProto.Person encode(byte[] body) throws InvalidProtocolBufferException {
        return UserInfoProto.Person.parseFrom(body);
    }

    private static UserInfoProto.Person createPerson() {
        UserInfoProto.Person.Builder builder = UserInfoProto.Person.newBuilder();
        builder.setName("roger");
        builder.setEmail("123@qq.com");
        builder.setId(1L);

        UserInfoProto.PhoneNumber.Builder phoneBuilder = UserInfoProto.PhoneNumber.newBuilder();
        phoneBuilder.setNumber("1234567890123");
        phoneBuilder.setType(UserInfoProto.PhoneType.MOBILE);

        List<UserInfoProto.PhoneNumber> numbers = new ArrayList<UserInfoProto.PhoneNumber>();
        numbers.add(phoneBuilder.build());

        builder.addAllPhones(numbers);
        return builder.build();
    }

    public static void main(String... args) throws InvalidProtocolBufferException {
        UserInfoProto.Person person = createPerson();
        System.out.println("before:");
        System.out.println("name:" + person.getEmail());
        System.out.println("phone:" + person.getPhones(0).getNumber());

        UserInfoProto.Person person2 = encode(decode(person));
        System.out.println("after:");
        System.out.println("name:" + person2.getEmail());
        System.out.println("phone:" + person2.getPhones(0).getNumber());
    }
}
