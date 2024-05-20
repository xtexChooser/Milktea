package net.pantasystem.milktea.data.infrastructure.notification.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import kotlinx.datetime.Instant

/**
 * キャッシュの塊を表すエンティティ
 */
@Entity(
    tableName = "notification_timelines"
)
data class NotificationTimelineEntity(
    val accountId: Long,
    
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)


// exclude types
@Entity(
    tableName = "notification_timeline_excluded_types",
    primaryKeys = ["timelineId", "type"]
)
data class NotificationTimelineExcludedTypeEntity(
    val timelineId: Long,
    val type: String,
)

// include types
@Entity(
    tableName = "notification_timeline_included_types",
    primaryKeys = ["timelineId", "type"]
)
data class NotificationTimelineIncludedTypeEntity(
    val timelineId: Long,
    val type: String,
)

// relation
data class NotificationTimelineRelation(
    @Embedded
    val timeline: NotificationTimelineEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId"
    )
    val excludedTypes: List<NotificationTimelineExcludedTypeEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId"
    )
    val includedTypes: List<NotificationTimelineIncludedTypeEntity>,
)

@Entity(
    tableName = "notification_timeline_items"
)
data class NotificationTimelineItemEntity(
    val timelineId: Long,
    val notificationId: String,
    val cachedAt: Instant,
)

@Dao
interface NotificationTimelineDAO {

    // insert
    @Insert
    suspend fun insert(entity: NotificationTimelineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExcludedTypes(entity: List<NotificationTimelineExcludedTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncludedTypes(entity: List<NotificationTimelineIncludedTypeEntity>)


    @Query(
        """
            SELECT * FROM notification_timelines
            JOIN (
                SELECT notification_timeline_excluded_types.timelineId FROM notification_timeline_excluded_types
                WHERE type IN (:excludeTypes)
                GROUP BY notification_timeline_excluded_types.timelineId
                HAVING COUNT(notification_timeline_excluded_types.timelineId) = COUNT(:excludeTypes)
            ) as excludes ON notification_timelines.id = excludes.timelineId
            JOIN (
                SELECT notification_timeline_included_types.timelineId FROM notification_timeline_included_types
                WHERE type IN (:includeTypes)
                GROUP BY notification_timeline_included_types.timelineId
                HAVING COUNT(notification_timeline_included_types.timelineId) = COUNT(:includeTypes)
            ) as includes ON notification_timelines.id = includes.timelineId
            WHERE accountId = :accountId
        """
    )
    suspend fun findByExcludeTypesAndIncludeTypes(
        accountId: Long,
        excludeTypes: List<String>,
        includeTypes: List<String>,
    ): List<NotificationTimelineRelation>

    // リレーションが何一つ設定されていないタイムラインを取得
    @Query(
        """
            SELECT * FROM notification_timelines
            WHERE id NOT IN (
                SELECT timelineId FROM notification_timeline_excluded_types
            ) AND id NOT IN (
                SELECT timelineId FROM notification_timeline_included_types
            )
            AND accountId = :accountId
        """
    )
    suspend fun findEmpty(accountId: Long): List<NotificationTimelineRelation>

    // include typesに完全一致し、exclude typesのリレーションが存在しないタイムラインを取得
    @Query(
        """
            SELECT * FROM notification_timelines
            JOIN (
                SELECT notification_timeline_included_types.timelineId FROM notification_timeline_included_types
                WHERE type IN (:includeTypes)
                GROUP BY notification_timeline_included_types.timelineId
                HAVING COUNT(notification_timeline_included_types.timelineId) = COUNT(:includeTypes)
            ) as includes ON notification_timelines.id = includes.timelineId
            WHERE id NOT IN (
                SELECT timelineId FROM notification_timeline_excluded_types
            )
            AND accountId = :accountId
        """
    )
    suspend fun findByIncludeTypes(accountId: Long, includeTypes: List<String>): List<NotificationTimelineRelation>

    // exclude typesに完全一致し、include typesのリレーションが存在しないタイムラインを取得
    @Query(
        """
            SELECT * FROM notification_timelines
            JOIN (
                SELECT notification_timeline_excluded_types.timelineId FROM notification_timeline_excluded_types
                WHERE type IN (:excludeTypes)
                GROUP BY notification_timeline_excluded_types.timelineId
                HAVING COUNT(notification_timeline_excluded_types.timelineId) = COUNT(:excludeTypes)
            ) as excludes ON notification_timelines.id = excludes.timelineId
            WHERE id NOT IN (
                SELECT timelineId FROM notification_timeline_included_types
            )
            AND accountId = :accountId
        """
    )
    suspend fun findByExcludeTypes(accountId: Long, excludeTypes: List<String>): List<NotificationTimelineRelation>

    // find by id
    @Query(
        """
            SELECT * FROM notification_timelines
            WHERE id = :id
        """
    )
    suspend fun findById(id: Long): NotificationTimelineRelation?
}