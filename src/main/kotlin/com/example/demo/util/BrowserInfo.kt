package com.example.demo.util

import jakarta.servlet.http.HttpServletRequest
import java.util.*


class BrowserInfo {
    fun browserInfo(request: HttpServletRequest): Map<String, String> {
        val agent = request.getHeader("USER-AGENT")

        val os = getClientOS(agent)
        val browser = getClientBrowser(agent)
        val ipAddress: String = getIpAddress(request)

        val map: MutableMap<String, String> = HashMap()
        map["ipAddress"] = ipAddress
        map["header"] = agent
        map["os"] = os
        map["browser"] = browser

        return map
    }

    fun getIpAddress(request: HttpServletRequest): String {
        var ipAddress: String? = request.getHeader("X-Forwarded-For")

        if (ipAddress.isNullOrBlank() || ipAddress.equals("unknown", ignoreCase = true)) {
            ipAddress = request.remoteAddr
        }

        ipAddress = ipAddress ?: ""
        return ipAddress.split(",")[0]
    }

    private fun getClientOS(uAgent: String): String {
        var os = ""
        val userAgent = uAgent.lowercase(Locale.getDefault())
        os = if (userAgent.indexOf("windows nt 10.0") > -1) {
            "Windows10"
        } else if (userAgent.indexOf("windows nt 6.1") > -1) {
            "Windows7"
        } else if (userAgent.indexOf("windows nt 6.2") > -1 || userAgent.indexOf("windows nt 6.3") > -1) {
            "Windows8"
        } else if (userAgent.indexOf("windows nt 6.0") > -1) {
            "WindowsVista"
        } else if (userAgent.indexOf("windows nt 5.1") > -1) {
            "WindowsXP"
        } else if (userAgent.indexOf("windows nt 5.0") > -1) {
            "Windows2000"
        } else if (userAgent.indexOf("windows nt 4.0") > -1) {
            "WindowsNT"
        } else if (userAgent.indexOf("windows 98") > -1) {
            "Windows98"
        } else if (userAgent.indexOf("windows 95") > -1) {
            "Windows95"
        } else if (userAgent.indexOf("iphone") > -1) {
            "iPhone"
        } else if (userAgent.indexOf("ipad") > -1) {
            "iPad"
        } else if (userAgent.indexOf("android") > -1) {
            "android"
        } else if (userAgent.indexOf("mac") > -1) {
            "mac"
        } else if (userAgent.indexOf("linux") > -1) {
            "Linux"
        } else {
            "Other"
        }
        return os
    }

    private fun getClientBrowser(userAgent: String): String {
        var browser = ""
        browser = if (userAgent.indexOf("Trident/7.0") > -1) {
            "ie11"
        } else if (userAgent.indexOf("MSIE 10") > -1) {
            "ie10"
        } else if (userAgent.indexOf("MSIE 9") > -1) {
            "ie9"
        } else if (userAgent.indexOf("MSIE 8") > -1) {
            "ie8"
        } else if (userAgent.indexOf("Chrome/") > -1) {
            "Chrome"
        } else if (userAgent.indexOf("Chrome/") == -1 && userAgent.indexOf("Safari/") >= -1) {
            "Safari"
        } else if (userAgent.indexOf("Firefox/") >= -1) {
            "Firefox"
        } else {
            "Other"
        }
        return browser
    }

}
