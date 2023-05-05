package com.example.userservice.Login.domain;

import lombok.*;

import javax.persistence.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USER_TB")
@Entity
public class UserEntity {
    @Id
    @Column(name = "USER_PK")
    private long userId;    //kakao user id

    @Column(name = "THUMBNAIL_IMAGE_URL")
    private String thumbnailImageUrl;

    @Column(name = "CHARACTER_NM")
    private String characterName;
    @Column(name = "EXPERIENCE_VALUE")
    private int experienceValue;
    @Column(name = "CHARACTER_IMAGE_URL")
    private String characterUrl;

    @Column(name = "STATE_MSG")
    private String stateMsg;
}
