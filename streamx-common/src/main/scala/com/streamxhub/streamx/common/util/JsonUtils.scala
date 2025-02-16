/*
 * Copyright (c) 2019 The StreamX Project
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamxhub.streamx.common.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.text.SimpleDateFormat
import scala.reflect.ClassTag

object JsonUtils extends Serializable {

  private[this] lazy val mapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    //忽略在json字符串中存在，在java类中不存在字段，防止错误。
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    //该属性设置主要是将忽略空bean转json错误
    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    //该属性设置主要是取消将对象的时间默认转换timesstamps(时间戳)形式
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    //所有日期都统一为以下样式：yyyy-MM-dd HH:mm:ss，这里可以不用我的DateTimeUtil.DATE_FORMAT，手动添加
    .setDateFormat(new SimpleDateFormat(DateUtils.fullFormat))

  def read[T](obj: AnyRef)(implicit clazz: ClassTag[T]): T = {
    obj match {
      case str: String => mapper.readValue(str, implicitly[ClassTag[T]].runtimeClass).asInstanceOf[T]
      case _ => mapper.readValue(write(obj), implicitly[ClassTag[T]].runtimeClass).asInstanceOf[T]
    }
  }

  def write(obj: AnyRef): String = mapper.writeValueAsString(obj)

  implicit class Unmarshal(jsonStr: String) {
    def fromJson[T](implicit clazz: ClassTag[T]): T = read[T](jsonStr)
  }

  implicit class Marshal(obj: AnyRef) {
    def toJson: String = write(obj)
  }

}

