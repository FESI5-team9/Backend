package com.fesi.mukitlist.domain.gathering;

import jakarta.persistence.Entity;
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
public class Keyword {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String keyword;

	@ManyToOne
	Gathering gathering;

	@Builder
	private Keyword(String keyword, Gathering gathering) {
		this.keyword = keyword;
		this.gathering = gathering;
	}

	public static Keyword of(String keyword, Gathering gathering) {
		return new Keyword(keyword, gathering);
	}

	@Override
	public String toString() {
		return keyword;
	}
}
