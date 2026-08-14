package com.zaa.cryptdroid.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * JsonUtil — JSON 格式化 / 压缩 / 校验 / 键值提取
 */
object JsonUtil {

    /** 校验并格式化 JSON（缩进 4 空格） */
    fun format(json: String): String {
        val trimmed = json.trim()
        val formatted = if (trimmed.startsWith("[")) {
            JSONArray(trimmed).toString(4)
        } else {
            JSONObject(trimmed).toString(4)
        }
        return formatted
    }

    /** 压缩 JSON（去掉所有空白） */
    fun compress(json: String): String {
        val trimmed = json.trim()
        return if (trimmed.startsWith("[")) {
            JSONArray(trimmed).toString()
        } else {
            JSONObject(trimmed).toString()
        }
    }

    /** 校验是否合法 JSON */
    fun isValid(json: String): Boolean {
        val trimmed = json.trim()
        return try {
            if (trimmed.startsWith("[")) JSONArray(trimmed) else JSONObject(trimmed)
            true
        } catch (e: JSONException) {
            false
        }
    }

    /** 提取所有键路径（"a.b.c" 格式） */
    fun listKeys(json: String): List<String> {
        val result = mutableListOf<String>()
        val trimmed = json.trim()
        try {
            if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                walk(JSONObject(trimmed), "", result)
            }
        } catch (_: JSONException) {
        }
        return result
    }

    private fun walk(obj: JSONObject, prefix: String, out: MutableList<String>) {
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val full = if (prefix.isEmpty()) key else "$prefix.$key"
            out.add(full)
            val value = obj.opt(key)
            if (value is JSONObject) {
                walk(value, full, out)
            } else if (value is JSONArray) {
                for (i in 0 until value.length()) {
                    val item = value.opt(i)
                    if (item is JSONObject) {
                        walk(item, "$full[$i]", out)
                    }
                }
            }
        }
    }
}
