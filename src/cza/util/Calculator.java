package cza.util;

import java.util.Stack;

public class Calculator {
	private static Calculator instance;

	public static Calculator getInstance(){
		if (instance == null)
			instance = new Calculator();
		return instance;
	}
	
	/**
	 * 利用java.util.Stack计算四则运算字符串表达式的值，如果抛出异常，则说明表达式有误，这里就没有控制
	 * java.util.Stack其实也是继承自java.util.Vector的。
	 * @param computeExpr 四则运算字符串表达式
	 * @return 计算结果
	 */
	public long compute(String computeExpr) {
		int length = computeExpr.length();
		int p;
		char mChar;
		int temp;
		long tempNumber = 0;
		boolean lastIsNumber = false;
		Stack<Long> numStack = new Stack<Long>();	//用来存放数字的栈
		Stack<Character> operStack = new Stack<Character>();	//存放操作符的栈
		for (p = 0; p < length; p++){
			mChar = computeExpr.charAt(p);
			if (mChar == ' ')
				continue;
			temp = parseNumber(mChar);
			if (temp != -1){
				tempNumber *= 10;
				tempNumber += temp;
				lastIsNumber = true;
			} else {
				//运算符
				if (lastIsNumber)
					numStack.push(tempNumber); //加入到数字栈中
				if (mChar == '(') {
					//左括号时加入括号操作符到栈顶
					if (lastIsNumber)
						operStack.push('*');
					operStack.push('(');
				} else if (mChar == ')') {
					 //右括号时, 把左括号跟右括号之间剩余的运算符都执行了
						while (operStack.peek() != '(') 
							compute(numStack, operStack);
						operStack.pop();//移除栈顶的左括号
				} else {
					if (mChar == '-' && !lastIsNumber) 
						//负号优先级高
						numStack.push(0l);
					else 
						while (!operStack.empty() && (getPriority(operStack.peek()) >= getPriority(mChar))) {
							compute(numStack, operStack);
						}
					//计算完后把当前操作符加入到操作栈中
					operStack.push(mChar);
				}
				lastIsNumber = false;
				tempNumber = 0;
			}
		}
		if (lastIsNumber)
			numStack.push(tempNumber); //最后一个数
		// 经过上面代码的遍历后最后的应该是nums里面剩两个数或三个数，operators里面剩一个或两个运算操作符
		while (!operStack.empty()) {
			compute(numStack, operStack);
		}
		return numStack.pop();
	}

	/**
	 * 取numStack的最顶上两个数字，operStack的最顶上一个运算符进行运算，然后把运算结果再放到numStack的最顶端
	 * @param numStack	数字栈
	 * @param operStack 操作栈
	 */
	private void compute(Stack<Long> numStack, Stack<Character> operStack) {
		long num2 = numStack.pop();
		long num1 = numStack.pop();
		long computeResult = compute(num1, num2,
			operStack.pop()); // 弹出操作栈最顶上的运算符进行计算
		numStack.push(computeResult); // 把计算结果重新放到队列的末端
	}
	
	private int getPriority(char operator){
		switch (operator){
			case '+':
			case '-':
				return 1;
			case '*':
			case '/':
				return 2;
			default:
				return 0;
		}
	}
	
	private long compute(long num1, long num2, char operator) {
		switch (operator){
			case '+':
				return num1 + num2;
			case '-':
				return num1 - num2;
			case '*':
				return num1 * num2;
			case '/':
				return num1 / num2;
			default:
				return 0;
		}
	}
	
	private int parseNumber(char ch){
		if ('0' <= ch && ch <= '9')
			return ch - '0';
		else 
			return -1;
	}
}

