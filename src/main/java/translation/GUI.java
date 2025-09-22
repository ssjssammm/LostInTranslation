package translation;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GUI {

    private final Translator translator;
    private final CountryCodeConverter countryConv;
    private final LanguageCodeConverter langConv;

    private JFrame frame;
    private JComboBox<String> languageCombo;
    private JList<String> countryList;
    private JLabel resultLabel;

    private final LinkedHashMap<String, String> displayLangToCode = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> displayCountryToCode = new LinkedHashMap<>();

    private static final Map<String,String> LANG_CODE_ALIASES = new HashMap<>() {{
        put("kr","ko");
    }};

    public GUI(Translator translator,
               CountryCodeConverter countryConv,
               LanguageCodeConverter langConv) {
        this.translator = translator;
        this.countryConv = countryConv;
        this.langConv = langConv;
        initData();
        initUI();
    }

    private void initData() {
        displayLangToCode.clear();
        List<String> langCodes = translator.getLanguageCodes();
        if (langCodes != null) {
            for (String c : langCodes) {
                String code = c == null ? "" : c.toLowerCase(java.util.Locale.ROOT);
                code = LANG_CODE_ALIASES.getOrDefault(code, code);
                if (code.isBlank()) continue;
                String name = langConv.fromLanguageCode(code);
                String display = (name == null || name.isBlank()) ? code : name;
                displayLangToCode.put(display, code);
            }
        }
        displayCountryToCode.clear();
        List<String> countryCodes = translator.getCountryCodes();
        if (countryCodes != null) {
            for (String a3 : countryCodes) {
                String code = a3 == null ? "" : a3.toLowerCase(java.util.Locale.ROOT);
                if (code.isBlank()) continue;
                String name = countryConv.fromCountryCode(code);
                String display = (name == null || name.isBlank()) ? code.toUpperCase(java.util.Locale.ROOT) : name;
                displayCountryToCode.put(display, code);
            }
        }
    }

    private void initUI() {
        frame = new JFrame("Country Name Translator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        Font base = new Font("Dialog", Font.PLAIN, 14);

        JPanel langRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        languageCombo = new JComboBox<>(displayLangToCode.keySet().toArray(new String[0]));
        languageCombo.setFont(base);
        JLabel langLabel = new JLabel("Language:");
        langLabel.setFont(base);
        langRow.add(langLabel);
        langRow.add(languageCombo);

        resultLabel = new JLabel("Translation: ", SwingConstants.CENTER);
        resultLabel.setFont(base);
        JPanel resultRow = new JPanel(new BorderLayout());
        resultRow.add(resultLabel, BorderLayout.CENTER);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(langRow);
        north.add(resultRow);
        frame.add(north, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String n : displayCountryToCode.keySet()) model.addElement(n);
        countryList = new JList<>(model);
        countryList.setFont(base);
        countryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listPane = new JScrollPane(countryList);
        listPane.setPreferredSize(new Dimension(360, 380));
        frame.add(listPane, BorderLayout.CENTER);

        languageCombo.addActionListener(e -> updateTranslation());
        countryList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateTranslation(); });

        if (languageCombo.getItemCount() > 0) languageCombo.setSelectedIndex(0);
        if (!model.isEmpty()) countryList.setSelectedIndex(0);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void updateTranslation() {
        String countryName = countryList.getSelectedValue();
        Object langDisplay = languageCombo.getSelectedItem();
        if (countryName == null || langDisplay == null) return;
        String alpha3 = displayCountryToCode.get(countryName).toLowerCase(java.util.Locale.ROOT);
        String langCode = displayLangToCode.get(langDisplay.toString()).toLowerCase(java.util.Locale.ROOT);
        String translated = translator.translate(alpha3, langCode);
        resultLabel.setText("Translation: " + (translated == null || translated.isBlank() ? "No translation found." : translated));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Translator t = new JSONTranslator("sample.json");
            CountryCodeConverter c = new CountryCodeConverter("country-codes.txt");
            LanguageCodeConverter l = new LanguageCodeConverter("language-codes.txt");
            new GUI(t, c, l);
        });
    }
}
