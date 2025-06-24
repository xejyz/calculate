import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class EquationCalculator {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> equations = new ArrayList<>();
        ArrayList<String> results = new ArrayList<>();

        while (true) {
            System.out.println("Введите уравнение (для выхода введите 'exit'):");
            String equation = scanner.nextLine();

            if (equation.equals("exit")) {
                break;
            }

            equations.add(equation);
            double result = calculateEquation(equation);
            results.add(String.valueOf(result));
        }

        System.out.println("Сохранить уравнения и результаты в файл? (yes/no)");
        String saveAnswer = scanner.nextLine();

        if (saveAnswer.equals("yes")) {
            System.out.println("Введите путь и имя файла для сохранения (например, C:\\path\\to\\file.txt):");
            String filePath = scanner.nextLine();

            saveEquationsAndResults(equations, results, filePath);
        } else {
            System.out.println("Уравнения и результаты не сохранены.");
        }
    }

    private static double calculateEquation(String equation) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < equation.length()) ? equation.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < equation.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(equation.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = equation.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    private static void saveEquationsAndResults(ArrayList<String> equations, ArrayList<String> results, String filePath) throws IOException {
        File file;
        if (filePath.isEmpty()) {
            file = new File("log.log");
        } else {
            file = new File(filePath);
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            for (int i = 0; i < equations.size(); i++) {
                fileWriter.write("Уравнение: " + equations.get(i) + "\n");
                fileWriter.write("Результат: " + results.get(i) + "\n\n");
            }
        }
        System.out.println("Уравнения и результаты сохранены в файл: " + file.getAbsolutePath());
    }
}