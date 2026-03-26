package com.lmen918.pda.data.repository

import com.lmen918.pda.data.local.dao.TagDao
import com.lmen918.pda.data.local.entity.TagEntity
import com.lmen918.pda.domain.model.Tag
import com.lmen918.pda.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> =
        tagDao.getAllTags().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertTag(tag: Tag): Long =
        tagDao.insertTag(tag.toEntity())

    override suspend fun updateTag(tag: Tag) =
        tagDao.updateTag(tag.toEntity())

    override suspend fun deleteTag(tag: Tag) =
        tagDao.deleteTag(tag.toEntity())

    override suspend fun getTagById(id: Long): Tag? =
        tagDao.getTagById(id)?.toDomain()

    private fun TagEntity.toDomain() = Tag(id = id, name = name, colorHex = colorHex)
    private fun Tag.toEntity() = TagEntity(id = id, name = name, colorHex = colorHex)
}
