package com.example.fasns.domain.post.repository;

import com.example.fasns.domain.post.dto.DailyPostCountDto;
import com.example.fasns.domain.post.dto.DailyPostCountRequest;
import com.example.fasns.domain.post.entity.Post;
import com.example.fasns.global.exception.ErrorCode;
import com.example.fasns.global.exception.SystemException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import utils.PageHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String TABLE = "Post";

    private static final String MEMBER_ID = "memberId";

    private static final RowMapper<Post> ROW_MAPPER = ((rs, rowNum) -> Post.builder()
            .id(rs.getLong("id"))
            .memberId(rs.getLong(MEMBER_ID))
            .contents(rs.getString("contents"))
            .likeCount(rs.getLong("likeCount"))
            .version(rs.getLong("version"))
            .createdDate(rs.getObject("createdDate", LocalDate.class))
            .createdAt(rs.getObject("createdAt", LocalDateTime.class))
            .build()
    );

    private static final RowMapper<DailyPostCountDto> DAILY_POST_COUNT_DTO_ROW_MAPPER = ((rs, rowNum) -> new DailyPostCountDto(
            rs.getLong(MEMBER_ID),
            rs.getObject("createdDate", LocalDate.class),
            rs.getLong("count")
    ));

    public List<DailyPostCountDto> groupByCreatedDate(DailyPostCountRequest request) {
        String sql = String.format("select createdDate, memberId, count(id) as count \n" +
                "from %s \n" +
                "where memberId = :memberId and createdDate between :firstDate and :lastDate \n" +
                "group by createdDate, memberId", TABLE);

        SqlParameterSource params = new BeanPropertySqlParameterSource(request);
        return namedParameterJdbcTemplate.query(sql, params, DAILY_POST_COUNT_DTO_ROW_MAPPER);
    }

    public Optional<Post> findById(Long postId, boolean requiredLock) {
        String sql = String.format("SELECT * FROM %s WHERE id = :postId", TABLE);
        if (requiredLock) {
            sql += " FOR UPDATE";
        }

        SqlParameterSource params = new MapSqlParameterSource().addValue("postId", postId);

        try {
            Post post = namedParameterJdbcTemplate.queryForObject(sql, params, ROW_MAPPER);
            return Optional.ofNullable(post);
        } catch (EmptyResultDataAccessException e) {
            throw new SystemException(ErrorCode.POST_NOT_FOUND);
        }
    }

    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable) {
        String sql = String.format("SELECT * \n" +
                "from %s \n" +
                "WHERE memberId = :memberId \n" +
                "ORDER BY %s \n" +
                "LIMIT :size \n" +
                "OFFSET :offset", TABLE, PageHelper.orderBy(pageable.getSort()));

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(MEMBER_ID, memberId)
                .addValue("size", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());


        List<Post> posts = namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
        return new PageImpl(posts, pageable, getCount(memberId));
    }

    private Long getCount(Long memberId) {
        String sql = String.format("SELECT count(id) FROM %s WHERE memberId = :memberId", TABLE);

        SqlParameterSource params = new MapSqlParameterSource().addValue(MEMBER_ID, memberId);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    public List<Post> findAllByInId(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        String sql = String.format("SELECT * FROM %s WHERE id in (:ids)", TABLE);

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByMemberIdInAndOrderByIdDesc(Long memberId, int size) {
        String sql = String.format("SELECT * \n" +
                "FROM %s \n" +
                "WHERE memberId = :memberId \n" +
                "ORDER BY id desc \n" +
                "LIMIT :size", TABLE);

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue(MEMBER_ID, memberId)
                .addValue("size", size);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByMemberIdInAndOrderByIdDesc(List<Long> memberIds, int size) {
        if(memberIds.isEmpty()) {
            return List.of();
        }

        String sql = String.format("SELECT * \n" +
                "FROM %s \n" +
                "WHERE memberId in (:memberIds) \n" +
                "ORDER BY id desc \n" +
                "LIMIT :size", TABLE);

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberIds", memberIds)
                .addValue("size", size);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByLessThanIdAndMemberIdInAndOrderByIdDesc(Long id, Long memberId, int size) {
        String sql = String.format("SELECT * \n" +
                "FROM %s \n" +
                "WHERE memberId = :memberId AND id < :id \n" +
                "ORDER BY id desc \n" +
                "LIMIT :size", TABLE);

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue(MEMBER_ID, memberId)
                .addValue("size", size);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByLessThanIdAndMemberIdInAndOrderByIdDesc(Long id, List<Long> memberIds, int size) {
        if (memberIds.isEmpty()) {
            return List.of();
        }
        String sql = String.format("SELECT * \n" +
                "FROM %s \n" +
                "WHERE memberId in (:memberIds) AND id < :id \n" +
                "ORDER BY id desc \n" +
                "LIMIT :size", TABLE);

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("memberIds", memberIds)
                .addValue("size", size);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public Post save(Post post) {
        if (post.getId() == null) {
            return insert(post);
        }
        return update(post);
    }

    public void deleteById(Long id) {
        String sql = String.format("DELETE FROM %s WHERE id = :id", TABLE);
        SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        namedParameterJdbcTemplate.update(sql, params);
    }

    public void bulkInsert(List<Post> posts) {
        String sql = String.format("INSERT INTO %s (memberId, contents, createdDate, createdAt) \n" +
                "VALUES (:memberId, :contents, :createdDate, :createdAt)", TABLE);

        SqlParameterSource[] params = posts.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sql, params);
    }

    private Post insert(Post post) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");

        SqlParameterSource params = new BeanPropertySqlParameterSource(post);
        long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return Post.builder()
                .id(id)
                .memberId(post.getMemberId())
                .contents(post.getContents())
                .createdAt(post.getCreatedAt())
                .createdDate(post.getCreatedDate())
                .build();
    }

    private Post update(Post post) {
        String sql = String.format("UPDATE %s SET " +
                "contents = :contents, \n" +
                "likeCount = :likeCount, \n" +
                "createdAt = :createdAt, \n" +
                "createdDate = :createdDate, \n" +
                "version = version + 1 \n" +
                "WHERE id = :id and version = :version", TABLE);

        SqlParameterSource params = new BeanPropertySqlParameterSource(post);
        int updatedCount = namedParameterJdbcTemplate.update(sql, params);

        if (updatedCount == 0) {
            throw new SystemException(ErrorCode.UPDATE_COUNT_ZERO);
        }

        return post;
    }

}
