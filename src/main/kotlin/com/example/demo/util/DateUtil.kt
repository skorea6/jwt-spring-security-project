package com.example.demo.util

import java.lang.Long
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object DateUtil {
    // 1일을 second로 환산한 값
    val ONE_DAY_SEC = (24 * 60 * 60).toLong()

    // 1일을 millisecond로 환산한 값
    val ONE_DAY_MS = ONE_DAY_SEC * 1000

    // 기본 날짜 포맷 (yyyy-MM-dd HH:mm:ss)
    val DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

    // yyyyMMddHHmmss
    val YYYYMMDDHHMMSS = "yyyyMMddHHmmss"

    // 날짜 포맷
    val DATE_FORMAT = "yyyy-MM-dd"

    // yyyyMMdd
    val DATE_FORMAT_WITHOUT_DASH = "yyyyMMdd"

    // yyyy-MM-dd HH:mm
    val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"

    // yyyyMMddHHmm
    val DATE_TIME_FORMAT_WITHOUT_DASH = "yyyyMMddHHmm"

    // HHmmss
    val TIME_FORMAT_WITHOUT_DASH = "HHmmss"

    // 기본 날짜 포맷 길이
    val DEFAUT_DATE_FORMAT_LEN = DEFAULT_DATE_FORMAT.length

    // 날짜 포맷 길이
    val DATE_FORMAT_LEN = DATE_FORMAT.length

    fun getCurrentTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    fun getCurrentDateAsString(): String {
        return getCurrentDateAsString(DEFAULT_DATE_FORMAT)
    }

    fun getCurrentDateAsString(format: String?): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format))
    }

    /**
     * 문자열을 날짜로 변환한다.
     *
     * @param dateString 날짜 문자열
     * @return 변환된 날짜
     */
    fun stringToDate(dateString: String?): LocalDate? {
        return stringToDate(dateString, null)
    }

    /**
     * 문자열을 날짜로 변환한다.
     *
     * @param dateString 날짜 문자열
     * @param format     변환할 포맷
     * @return 변환된 날짜
     */
    private fun stringToDate(dateString: String?, format: String?): LocalDate? {
        if (dateString == null) return null
        val newFormat = makeFormat(dateString, format)
        println("newFormat: $newFormat")
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(newFormat))
    }

    private fun makeFormat(dateString: String, format: String?): String {
        val newFormat: String = if (format != null) {
            format
        } else {
            val strLen = dateString.length
            if (strLen == DATE_FORMAT_LEN) {
                DATE_FORMAT
            } else {
                DATE_FORMAT_WITHOUT_DASH
            }
        }
        return newFormat
    }

    /**
     * 날짜를 문자열로 변환한다.
     *
     * @param date   날짜
     * @param format 포맷
     * @return 문자열 날짜
     */
    fun dateToString(date: LocalDateTime, format: String?): String {
        var format = format
        if (format == null) {
            format = DEFAULT_DATE_FORMAT
        }
        return date.format(DateTimeFormatter.ofPattern(format))
    }

    /**
     * 두 날짜의 차이(일수)를 구한다. <br></br>
     * lastDate - firstDate의 일수
     *
     * @param firstDate
     * @param lastDate
     * @return
     */
    fun diffDate(firstDate: LocalDate?, lastDate: LocalDate?): kotlin.Long {
        return ChronoUnit.DAYS.between(firstDate, lastDate)
    }

    fun diffDateTime(firstDateTime: LocalDateTime?, lastDateTime: LocalDateTime?): kotlin.Long {
        return ChronoUnit.DAYS.between(firstDateTime, lastDateTime)
    }

    /**
     * Unix epoch seconds 를 date로 변환한다.
     *
     * @param long timestamp ex) 1522313221
     * @return
     */
    fun getTimestampToDate(timestamp: kotlin.Long): String? {
        return try {
            val date = Date(timestamp * 1000L)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.timeZone = TimeZone.getTimeZone("GMT+9")
            sdf.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * date를 unix ephoch seconds로 변환한다.
     *
     * @param String date (yyyy-mm-dd HH:mm:ss)
     * @return
     */
    fun getDateToTimestamp(dateStr: String?): String? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = format.parse(dateStr)
            val timeStamp = date.time
            Long.toString(timeStamp / 1000)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 현재날짜 -standardDay 일자보다 체크날짜가 이후날짜인지 확인
     *
     * @param
     * @return
     * @throws ParseException
     */
    @Throws(ParseException::class)
    fun standardLateChk(checkDt: String?, standardDay: Int): Boolean {
        var standardDay = standardDay
        val transFormats = SimpleDateFormat("yyyy-MM-dd")
        standardDay = standardDay * -1 // 정수 음수변환
        val day = Calendar.getInstance()
        day.add(Calendar.DATE, standardDay)
        val compare = day.time.compareTo(transFormats.parse(checkDt))
        return if (compare < 0) {
            true
        } else {
            false
        }
    }

    /**
     * 현재시간에 지정분을 추가한다.
     *
     * @param long plusMinutes ex) 30 @return @throws
     */
    fun getDatePlusTime(plusMinutes: kotlin.Long): String? {
        val now = LocalDateTime.now()
        return now.plusMinutes(plusMinutes).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    /**
     * 일시에 하이픈, 콜론을 추가한다.
     *
     * @param String yyyyMMddHHmmss ex) 20200101120030
     * @return String ex)2020-01-01 12:00:30
     */
    fun getFormatDateTime(yyyyMMddHHmmss: String): String? {
        return (yyyyMMddHHmmss.substring(0, 4) + "-" + yyyyMMddHHmmss.substring(
            4,
            6
        ) + "-" + yyyyMMddHHmmss.substring(6, 8) + " "
                + yyyyMMddHHmmss.substring(8, 10) + ":" + yyyyMMddHHmmss.substring(
            10,
            12
        ) + ":" + yyyyMMddHHmmss.substring(12, 14))
    }

    /**
     * 일시에 하이픈, 콜론을 추가한다.
     *
     * @param String yyyy-MM-dd HH:mm:ss ex) 2020-01-01 12:00:30
     * @return String ex)20200101120030
     */
    fun removeHyphonDateTime(yyyyMMddHHmmss: String): String? {
        return yyyyMMddHHmmss.replace("-", "").replace(" ", "").replace(":", "").trim { it <= ' ' }
    }

    fun isVaildlDateFormat(dateStr: String?, nullOk: Boolean): Boolean {
        //
        if (nullOk == true && dateStr == null) {
            return true
        }
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT_WITHOUT_DASH))
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun isVaildDateTimeFormat(dateStr: String?, nullOk: Boolean): Boolean {
        if (nullOk && dateStr == null) {
            return true
        }
        try {
            val strLen = dateStr!!.length
            if (strLen == DEFAUT_DATE_FORMAT_LEN) {
                LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT))
            } else {
                LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(YYYYMMDDHHMMSS))
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun main() {
        println("getCurrentTime: " + getCurrentTime())
        println("getCurrentDateAsString1: " + getCurrentDateAsString())
        println("getCurrentDateAsString2: " + getCurrentDateAsString(DEFAULT_DATE_FORMAT))
        println("getCurrentDateAsString3: " + getCurrentDateAsString(YYYYMMDDHHMMSS))
        println("getCurrentDateAsString4: " + getCurrentDateAsString(DATE_FORMAT))
        println("getCurrentDateAsString5: " + getCurrentDateAsString(DATE_FORMAT_WITHOUT_DASH))
        println("getCurrentDateAsString7: " + getCurrentDateAsString(DATE_TIME_FORMAT))
        println("getCurrentDateAsString8: " + getCurrentDateAsString(DATE_TIME_FORMAT_WITHOUT_DASH))
        println("getCurrentDateAsString9: " + getCurrentDateAsString(TIME_FORMAT_WITHOUT_DASH))
        val dateTime = LocalDateTime.now()
        println("dateToString1: " + dateToString(dateTime, DEFAULT_DATE_FORMAT))
        println("dateToString2: " + dateToString(dateTime, YYYYMMDDHHMMSS))
        println("dateToString3: " + dateToString(dateTime, DATE_FORMAT))
        println("dateToString4: " + dateToString(dateTime, DATE_FORMAT_WITHOUT_DASH))
        println("dateToString5: " + dateToString(dateTime, DATE_TIME_FORMAT))
        println("dateToString6: " + dateToString(dateTime, DATE_TIME_FORMAT_WITHOUT_DASH))
        println("dateToString7: " + dateToString(dateTime, TIME_FORMAT_WITHOUT_DASH))
        println("stringToDate1: " + stringToDate("2021-01-19"))
        println("stringToDate2: " + stringToDate("20210119"))
        val firstDate: LocalDate? = stringToDate("2020-01-01", DATE_FORMAT)
        val lastDate = LocalDate.now()
        println("diffDate1: " + diffDate(firstDate, lastDate))
        val firstDateTime = LocalDateTime.parse("2020-01-01T12:34:56", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val lastDateTime = LocalDateTime.now()
        println("diffDate2: " + diffDateTime(firstDateTime, lastDateTime))
        println("isVaildlDateFormat: " + isVaildlDateFormat("20210119", false))
        println("isVaildDateTimeFormat1: " + isVaildDateTimeFormat("2021-01-21 18:14:20", false))
        println("isVaildDateTimeFormat2: " + isVaildDateTimeFormat("20210121181420", false))
    }
}