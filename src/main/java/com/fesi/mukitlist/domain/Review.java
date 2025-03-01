package com.fesi.mukitlist.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.gathering.Gathering;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private int score;
	private String comment;

	@CreatedDate
	private LocalDateTime createdAt;

	@ManyToOne
	private Gathering gathering;

	@ManyToOne
	private User user;

	@Builder
	private Review(int score, String comment, LocalDateTime createdAt, Gathering gathering, User user) {
		this.score = score;
		this.comment = comment;
		this.createdAt = createdAt;
		this.gathering = gathering;
		this.user = user;
	}

	public static Review of(int score, String comment, Gathering gathering, User user) {
		return Review.builder()
			.score(score)
			.comment(comment)
			.gathering(gathering)
			.user(user)
			.build();
	}
}
