package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import study.querydsl.entity.Member;

import java.util.List;

/**
 * 인터페이스 지원 - QuerydslPredicateExecutor
 * 공식 URL: https://docs.spring.io/spring-data/jpa/docs/2.2.3.RELEASE/reference/html/
 * #core.extensions.querydsl
 *
 * 한계점
 * 조인X (묵시적 조인은 가능하지만 left join이 불가능하다.)
 * 클라이언트가 Querydsl에 의존해야 한다. 서비스 클래스가 Querydsl이라는 구현 기술에 의존해야 한다.
 * 복잡한 실무환경에서 사용하기에는 한계가 명확하다.
 */

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
    //select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}
