package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.isEmpty;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;
// extends QuerydslRepositorySupport
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    /**
     * Creates a new {@link QuerydslRepositorySupport} instance for the given domain type.
     * QuerydslRepositorySupport가 추상클래스이기 떄문에 아래에 주석처리한 부분을 제공을 해줌
     *
     * 장점
     * getQuerydsl().applyPagination() 스프링 데이터가 제공하는 페이징을 Querydsl로 편리하게 변환
     * EntityManager 제공
     *
     * 한계
     * Querydsl 3.x 버전을 대상으로 만듬
     * Querydsl 4.x에 나온 JPAQueryFactory로 시작할 수 없음
     * select로 시작할 수 없음 (from으로 시작해야함)
     * QueryFactory 를 제공하지 않음
     * 스프링 데이터 Sort 기능이 정상 동작하지 않음
     *
     */
//    public MemberRepositoryImpl() {
//        super(Member.class);
//    }
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    //회원명, 팀명, 나이(ageGoe, ageLoe)
    public List<MemberTeamDto> search(MemberSearchCondition condition) {

//        List<MemberTeamDto> result = from(member)
//                .leftJoin(member.team, team)
//                .where(usernameEq(condition.getUsername()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe()))
//                .select(new QMemberTeamDto(
//                        member.id,
//                        member.username,
//                        member.age,
//                        team.id,
//                        team.name))
//                .fetch();


        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }

    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition,
                                                Pageable pageable) {
        List<MemberTeamDto> results = (List<MemberTeamDto>) queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        // long total = results.stream().count();
        // fetchResult나 fetchCount는 사용하지않고있으므로
        // fetch와 stream.count를 사용해 반환하는 식으로 진행하고자함.

        // return new PageImpl<>(results, pageable, total);

        // PageableExecutionUtils를 이용한 countQuery 쿼리가 생략가능한 경우
        // 1. 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 적을경우
        // 2. 마지막 페이지 일 때
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );
        return PageableExecutionUtils.getPage(results, pageable, ()-> countQuery.fetch().size());
    }

    /**
     * 복잡한 페이징
     * 데이터 조회 쿼리와, 전체 카운트 쿼리를 분리
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition,
                                                 Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long total = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch().size();
        // fetchCount() 대신 fetch().size() 로 동일한 결과를 얻을 수 있다고 설명한다.
        // 서비스의 크기에 따라 적절한 방법으로 count 를 받아와야 하므로 이에 대한 고민이 필요할 것 같다.
        return new PageImpl<>(content, pageable, total);
    }

}