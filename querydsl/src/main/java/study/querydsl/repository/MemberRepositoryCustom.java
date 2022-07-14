package study.querydsl.repository;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    // 사용자 정의 인터페이스
    List<MemberTeamDto> search(MemberSearchCondition condition);
}