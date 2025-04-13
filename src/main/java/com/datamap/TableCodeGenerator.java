package com.datamap;

import java.util.Scanner;

public class TableCodeGenerator {
    /**
     * 根据表名生成代码片段
     * @param tableName 表名
     * @return 生成的代码片段
     */
    public static String generateCode(String tableName) {
        // 将表名作为类名，全小写作为变量名
        String variableName = tableName.toLowerCase();

        StringBuilder code = new StringBuilder();
        code.append("        ").append(tableName).append(" ")
                .append(variableName).append(" = new ")
                .append(tableName).append("();\n");
        code.append("        PgSink.set.addAll(")
                .append(variableName)
                .append(".getComputationalLogicSet());\n");

        return code.toString();
    }

    public static void main(String[] args) {
        System.out.println("请输入表名，每行一个。输入空行结束。");

        Scanner scanner = new Scanner(System.in);
        StringBuilder result = new StringBuilder();

        // 读取输入行，直到输入空行
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // 如果输入空行，则结束输入
            if (line.trim().isEmpty()) {
                break;
            }

            String code = generateCode(line);
            result.append(code).append("\n");
        }

        scanner.close();
        System.out.println("\n生成的代码：\n");
        System.out.println(result );
    }
}