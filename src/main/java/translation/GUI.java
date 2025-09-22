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
                String name = langConv.fromLanguageCode(c);
                String display = (name == null || name.isBlank()) ? c : name + " (" + c + ")";
                displayLangToCode.put(display, c);
            }
        }
        displayCountryToCode.clear();
        List<String> countryCodes = translator.getCountryCodes();
        if (countryCodes != null) {
            for (String a3 : countryCodes) {
                String name = countryConv.fromCountryCode(a3);
                String display = (name == null || name.isBlank()) ? a3.toUpperCase(Locale.ROOT) : name;
                displayCountryToCode.put(display, a3);
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
        int h = languageCombo.getPreferredSize().height;
        FontMetrics fm = languageCombo.getFontMetrics(base);
        int w = fm.charWidth('M') * 8 + 96;
        languageCombo.setPreferredSize(new Dimension(w, h));
        langRow.add(langLabel);
        langRow.add(languageCombo);

        resultLabel = new JLabel("Translation: ", SwingConstants.CENTER);
        resultLabel.setFont(base);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(langRow);
        north.add(resultLabel);
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
        String alpha3 = displayCountryToCode.get(countryName);
        String langCode = displayLangToCode.get(langDisplay.toString());
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
