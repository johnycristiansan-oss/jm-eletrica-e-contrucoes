package br.com.jmeletrica.orcamentos

import org.json.JSONObject

enum class VerificationStatus { ESTIMATE, PROFESSIONAL_CHECKED, CONFIRMED }

data class TravelCost(
    val professionalDefined: Double = 0.0,
    val toll: Double = 0.0,
    val parking: Double = 0.0,
    val other: Double = 0.0
) {
    val total: Double get() = professionalDefined + toll + parking + other
}

data class QuoteDraft(
    val id: String,
    val service: String,
    val locationType: String,
    val measurements: String = "",
    val photosCount: Int = 0,
    val verification: VerificationStatus = VerificationStatus.ESTIMATE,
    val travelCost: TravelCost = TravelCost(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("service", service)
        put("locationType", locationType)
        put("measurements", measurements)
        put("photosCount", photosCount)
        put("verification", verification.name)
        put("createdAt", createdAt)
        put("travelCost", JSONObject().apply {
            put("professionalDefined", travelCost.professionalDefined)
            put("toll", travelCost.toll)
            put("parking", travelCost.parking)
            put("other", travelCost.other)
            put("total", travelCost.total)
        })
    }
}
