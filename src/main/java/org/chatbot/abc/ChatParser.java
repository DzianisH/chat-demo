package org.chatbot.abc;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by DzianisH on 07.03.2017.
 */
public class ChatParser {

	public static void main(String[] args) throws IOException {
//		step1();
//		step2();
		step3();
	}

	public static void step3() throws IOException {
		Path path = Paths.get("d:/Projects/idea/chat-bot/src/main/resources/dialogs2.txt");
		Files.lines(path)
				.flatMap(s -> Arrays.stream(s.split(" ")))
				.distinct()
				.sorted()
				.forEach(System.out::println);
	}


	public static void step2() throws IOException {
		Path path = Paths.get("d:/Projects/idea/chat-bot/src/main/resources/dialogs.txt");
		PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(
				"d:/Projects/idea/chat-bot/src/main/resources/dialogs1.txt"
		)));
		Files.lines(path)
				.map(ChatParser::prepareString)
				.forEach(out::println);
		out.flush();
		out.close();
	}

	public static String prepareString(String str){
		str = str.toLowerCase();
		return str.replaceAll("[_]+", "")
				.replaceAll("[\\W]+", " ")
				.replaceAll("[\\d]+", "number");
	}

	public static void step1() throws IOException {
		Path path = Paths.get("d:/Projects/idea/chat-bot/src/main/resources/movie_lines.txt");
		List<Replica> replicas = Files.lines(path)
				.map(ChatParser::textToReplica)
				.collect(Collectors.toList());

		PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(
				"d:/Projects/idea/chat-bot/src/main/resources/dialogs.txt"
		)));


		int lastUId = -3;
		String lastText = "";
		for (Replica replica : replicas){
			if(lastUId != replica.userId){

				out.println(lastText.trim());

				lastText = replica.text;
				lastUId = replica.userId;
			} else {
				lastText += " " + replica.text;
			}
		}
		out.println(lastText.trim());

		out.flush();
		out.close();
	}

	static class Replica{
		int userId;
		String text;

		public String toString(){
			return userId + ") " + text;
		}
	}
	private static Replica textToReplica(String text){
		final String spliter = "+++$+++";
		int i = text.lastIndexOf(spliter);
		String meta = text.substring(0, i + spliter.length());
		String chat = text.substring(i + spliter.length() + 1, text.length());

		Replica replica = new Replica();
		replica.text = chat;
		replica.userId = extractUserId(meta);
		return replica;
	}


	private static int extractUserId(String meta) {
		for (String chunk : meta.split(" ")){
			if(chunk.startsWith("u")){
				return Integer.parseInt(chunk.substring(1));
			}
		}
		throw  new Error("XXX  " + meta);
//		return -1;
	}
}
