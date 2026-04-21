package com.codeit.mission.deokhugam.user.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET status = 'DELETED', updated_at = NOW() WHERE id = ?")
@SQLRestriction("status != 'DELETED'")
public class User extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status;


  @Builder
  public User(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
    this.status = UserStatus.ACTIVE;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }
}
