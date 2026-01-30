package com.example.telemedicine;

import java.util.HashMap;
import java.util.Map;

/**
 * Realistic medication database with drug information for prescription validation
 * Contains essential drug information used in real medical practice
 */
public class MedicationDatabase {
    
    // Static medication database with common medications and their properties
    private static final Map<String, MedicationInfo> MEDICATIONS = new HashMap<>();
    
    static {
        // Common medications with realistic medical information
        MEDICATIONS.put("Paracetamol", new MedicationInfo(
            "Paracetamol",
            "Acetaminophen",
            "Analgesic, Antipyretic",
            "Oral tablet, Oral suspension, IV",
            "500mg, 650mg, 1000mg",
            "Adult: 500-1000mg every 4-6 hours PRN, max 4g/day",
            "Child: 10-15mg/kg every 4-6 hours PRN",
            "Hepatic impairment: Reduce dose, max 2-3g/day",
            "Contraindicated in severe liver disease",
            "Common: Nausea, rash; Rare: Hepatotoxicity",
            "CNS depression with alcohol",
            "",
            "Pregnancy Category B"
        ));
        
        MEDICATIONS.put("Ibuprofen", new MedicationInfo(
            "Ibuprofen",
            "NSAID",
            "Analgesic, Anti-inflammatory, Antipyretic",
            "Oral tablet, Oral suspension, Topical gel",
            "200mg, 400mg, 600mg, 800mg",
            "Adult: 200-400mg every 4-6 hours PRN, max 3.2g/day",
            "Child: 5-10mg/kg every 6-8 hours PRN",
            "Renal impairment: Use with caution, reduce dose",
            "Contraindicated in active peptic ulcer disease",
            "Common: GI upset, headache; Rare: GI bleeding, renal failure",
            "Increased risk with anticoagulants, corticosteroids",
            "",
            "Pregnancy Category C (avoid in 3rd trimester)"
        ));
        
        MEDICATIONS.put("Amoxicillin", new MedicationInfo(
            "Amoxicillin",
            "Penicillin antibiotic",
            "Antibacterial",
            "Oral capsule, Oral suspension, IV",
            "250mg, 500mg, 875mg",
            "Adult: 500mg every 8 hours or 875mg every 12 hours for 7-10 days",
            "Child: 20-40mg/kg/day divided every 8 hours",
            "Renal impairment: Adjust dose based on creatinine clearance",
            "Contraindicated in penicillin allergy",
            "Common: Diarrhea, nausea; Rare: Anaphylaxis, Stevens-Johnson syndrome",
            "May reduce efficacy of oral contraceptives",
            "",
            "Pregnancy Category B"
        ));
        
        MEDICATIONS.put("Azithromycin", new MedicationInfo(
            "Azithromycin",
            "Macrolide antibiotic",
            "Antibacterial",
            "Oral tablet, Oral suspension, IV",
            "250mg, 500mg, 600mg",
            "Adult: 500mg day 1, then 250mg daily for 4 days (Z-Pak)",
            "Child: 10mg/kg day 1, then 5mg/kg daily for 4 days",
            "Hepatic impairment: Use with caution",
            "Contraindicated in known hypersensitivity",
            "Common: GI upset, headache; Rare: QT prolongation, hepatotoxicity",
            "Increased levels with CYP3A4 inhibitors",
            "",
            "Pregnancy Category B"
        ));
        
        MEDICATIONS.put("Lisinopril", new MedicationInfo(
            "Lisinopril",
            "ACE inhibitor",
            "Antihypertensive, Heart failure",
            "Oral tablet",
            "2.5mg, 5mg, 10mg, 20mg, 40mg",
            "Adult: Start 5-10mg daily, max 40mg daily",
            "Child: Not recommended <6 years",
            "Renal impairment: Start low, monitor renal function",
            "Contraindicated in pregnancy, bilateral renal artery stenosis",
            "Common: Cough, dizziness; Rare: Angioedema, hyperkalemia",
            "Avoid with potassium-sparing diuretics, NSAIDs",
            "",
            "Pregnancy Category D"
        ));
        
        MEDICATIONS.put("Metformin", new MedicationInfo(
            "Metformin",
            "Biguanide",
            "Antidiabetic",
            "Oral tablet, Extended-release tablet",
            "500mg, 850mg, 1000mg",
            "Adult: Start 500mg twice daily, max 2000mg twice daily",
            "Child: Not recommended <10 years",
            "Renal impairment: Contraindicated if eGFR <30",
            "Contraindicated in severe renal impairment, metabolic acidosis",
            "Common: GI upset, metallic taste; Rare: Lactic acidosis",
            "Avoid with contrast dye, alcohol excess",
            "",
            "Pregnancy Category B"
        ));
        
        MEDICATIONS.put("Atorvastatin", new MedicationInfo(
            "Atorvastatin",
            "Statins",
            "Lipid-lowering",
            "Oral tablet",
            "10mg, 20mg, 40mg, 80mg",
            "Adult: Start 10-20mg daily, max 80mg daily",
            "Child: Not recommended <10 years",
            "Hepatic impairment: Use with caution",
            "Contraindicated in active liver disease",
            "Common: Myalgia, headache; Rare: Rhabdomyolysis, hepatotoxicity",
            "Increased risk with CYP3A4 inhibitors, fibrates",
            "",
            "Pregnancy Category X"
        ));
        
        MEDICATIONS.put("Omeprazole", new MedicationInfo(
            "Omeprazole",
            "Proton pump inhibitor",
            "Anti-ulcer",
            "Oral capsule, Oral suspension",
            "10mg, 20mg, 40mg",
            "Adult: 20-40mg daily for 4-8 weeks",
            "Child: 10-20mg daily based on weight",
            "Hepatic impairment: Reduce dose",
            "Contraindicated in known hypersensitivity",
            "Common: Headache, diarrhea; Rare: Hypomagnesemia, fractures",
            "May reduce absorption of clopidogrel, iron",
            "",
            "Pregnancy Category C"
        ));
    }
    
    /**
     * Get medication information by generic name
     */
    public static MedicationInfo getMedicationInfo(String medicationName) {
        return MEDICATIONS.get(medicationName);
    }
    
    /**
     * Search medications by partial name
     */
    public static MedicationInfo[] searchMedications(String query) {
        if (query == null || query.trim().isEmpty()) {
            return MEDICATIONS.values().toArray(new MedicationInfo[0]);
        }
        
        String searchTerm = query.toLowerCase();
        java.util.List<MedicationInfo> results = new java.util.ArrayList<>();
        
        for (MedicationInfo med : MEDICATIONS.values()) {
            if (med.getGenericName().toLowerCase().contains(searchTerm) ||
                med.getBrandName().toLowerCase().contains(searchTerm) ||
                med.getDrugClass().toLowerCase().contains(searchTerm)) {
                results.add(med);
            }
        }
        
        return results.toArray(new MedicationInfo[0]);
    }
    
    /**
     * Check for potential drug interactions
     */
    public static String checkDrugInteractions(Prescription.Medication[] medications) {
        if (medications == null || medications.length <= 1) {
            return "No interactions detected";
        }
        
        // Simple interaction checking logic
        StringBuilder interactions = new StringBuilder();
        
        for (int i = 0; i < medications.length; i++) {
            for (int j = i + 1; j < medications.length; j++) {
                String interaction = checkPairwiseInteraction(medications[i], medications[j]);
                if (interaction != null && !interaction.isEmpty()) {
                    if (interactions.length() > 0) {
                        interactions.append("\n");
                    }
                    interactions.append("⚠️ Interaction: ").append(medications[i].getName())
                               .append(" + ").append(medications[j].getName())
                               .append(" - ").append(interaction);
                }
            }
        }
        
        return interactions.length() > 0 ? interactions.toString() : "No significant interactions detected";
    }
    
    /**
     * Check pairwise drug interaction
     */
    private static String checkPairwiseInteraction(Prescription.Medication med1, Prescription.Medication med2) {
        String name1 = med1.getName().toLowerCase();
        String name2 = med2.getName().toLowerCase();
        
        // Example interactions (simplified for demo)
        if ((name1.contains("ibuprofen") && name2.contains("lisinopril")) ||
            (name1.contains("lisinopril") && name2.contains("ibuprofen"))) {
            return "May reduce antihypertensive effect of lisinopril; increased risk of renal impairment";
        }
        
        if ((name1.contains("metformin") && name2.contains("azithromycin")) ||
            (name1.contains("azithromycin") && name2.contains("metformin"))) {
            return "Potential increased risk of lactic acidosis";
        }
        
        if ((name1.contains("atorvastatin") && name2.contains("azithromycin")) ||
            (name1.contains("azithromycin") && name2.contains("atorvastatin"))) {
            return "Increased atorvastatin levels; risk of myopathy/rhabdomyolysis";
        }
        
        return null;
    }
    
    /**
     * Validate dosage against medical guidelines
     */
    public static String validateDosage(Prescription.Medication medication, int patientAge, boolean hasRenalImpairment, boolean hasHepaticImpairment) {
        MedicationInfo info = getMedicationInfo(medication.getName());
        if (info == null) {
            return "Medication not found in database";
        }
        
        StringBuilder warnings = new StringBuilder();
        
        // Age-based validation
        if (patientAge < 18 && info.getPediatricDosing() != null && !info.getPediatricDosing().isEmpty()) {
            // Check if dose is appropriate for age
            if (medication.getDosage() != null && !medication.getDosage().isEmpty()) {
                if (medication.getDosage().contains("mg") && !medication.getDosage().contains("kg")) {
                    warnings.append("⚠️ Pediatric dose should be weight-based (mg/kg). ");
                }
            }
        }
        
        // Renal impairment validation
        if (hasRenalImpairment && info.getRenalAdjustment() != null && !info.getRenalAdjustment().isEmpty()) {
            warnings.append("⚠️ Renal impairment: ").append(info.getRenalAdjustment()).append(" ");
        }
        
        // Hepatic impairment validation
        if (hasHepaticImpairment && info.getHepaticAdjustment() != null && !info.getHepaticAdjustment().isEmpty()) {
            warnings.append("⚠️ Hepatic impairment: ").append(info.getHepaticAdjustment()).append(" ");
        }
        
        // Contraindication check
        if (info.getContraindications() != null && !info.getContraindications().isEmpty()) {
            warnings.append("❗ Contraindicated: ").append(info.getContraindications()).append(" ");
        }
        
        return warnings.length() > 0 ? warnings.toString().trim() : "Dosage appears appropriate";
    }
    
    /**
     * Medication information class
     */
    public static class MedicationInfo {
        private final String brandName;
        private final String genericName;
        private final String drugClass;
        private final String dosageForms;
        private final String availableStrengths;
        private final String adultDosing;
        private final String pediatricDosing;
        private final String renalAdjustment;
        private final String hepaticAdjustment;
        private final String contraindications;
        private final String adverseEffects;
        private final String drugInteractions;
        private final String pregnancyCategory;
        
        public MedicationInfo(String brandName, String genericName, String drugClass,
                             String dosageForms, String availableStrengths,
                             String adultDosing, String pediatricDosing,
                             String renalAdjustment, String hepaticAdjustment,
                             String contraindications, String adverseEffects,
                             String drugInteractions, String pregnancyCategory) {
            this.brandName = brandName;
            this.genericName = genericName;
            this.drugClass = drugClass;
            this.dosageForms = dosageForms;
            this.availableStrengths = availableStrengths;
            this.adultDosing = adultDosing;
            this.pediatricDosing = pediatricDosing;
            this.renalAdjustment = renalAdjustment;
            this.hepaticAdjustment = hepaticAdjustment;
            this.contraindications = contraindications;
            this.adverseEffects = adverseEffects;
            this.drugInteractions = drugInteractions;
            this.pregnancyCategory = pregnancyCategory;
        }
        
        // Getters
        public String getBrandName() { return brandName; }
        public String getGenericName() { return genericName; }
        public String getDrugClass() { return drugClass; }
        public String getDosageForms() { return dosageForms; }
        public String getAvailableStrengths() { return availableStrengths; }
        public String getAdultDosing() { return adultDosing; }
        public String getPediatricDosing() { return pediatricDosing; }
        public String getRenalAdjustment() { return renalAdjustment; }
        public String getHepaticAdjustment() { return hepaticAdjustment; }
        public String getContraindications() { return contraindications; }
        public String getAdverseEffects() { return adverseEffects; }
        public String getDrugInteractions() { return drugInteractions; }
        public String getPregnancyCategory() { return pregnancyCategory; }
    }
}