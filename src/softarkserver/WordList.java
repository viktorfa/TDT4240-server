package softarkserver;

import java.util.ArrayList;

/**
 *  Class for the word lists used in the game. Contains a name and list of words in the format <word>:<keyboard word>.
 *	<word> is the the word to draw
 *	<keyboard word> is the keyboard that is displayed to the user. 
 *	ex. car:hcgagr 
 */
public class WordList {
	private String name;
	private ArrayList<String> words;
	
	/**
	 * @param name of the list
	 * @param words list of words in the <word>:<keyboard word> format
	 */
	public WordList(String name, String... words){
		this.name = name;
		this.words = new ArrayList<String>();
		
		for(String word : words){
			this.words.add(word);
		}
	}
	
	/**
	 * @param index word index
	 * @return <keyboard word> 
	 */
	public String getKeyboardWord(int index){
		if(index < words.size()){
			String splittedWords[] = words.get(index).split(":");
			if(splittedWords.length >= 2){
				return splittedWords[1];
			}
			return "Error, this word has no keyboard layout.";
		}
		return "Error, out of boundce";
	}
	
	/**
	 * @param index word
	 * @return <word>
	 */
	public String getAnswerWord(int index){
		if(index < words.size()){
			String splittedWords[] = words.get(index).split(":");
			return splittedWords[0];
		}
		return "Error, out of boundce";
	}
	
	/**
	 * @return name of the list
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * @return num words in the list
	 */
	public int getSize(){
		return words.size();
	}
	
	/**
	 * @param index word
	 * @return <word>:<keyboard word>
	 */
	public String getWord(int index){
		if(index < words.size()){
			return words.get(index);
		}
		return "Index out of boundce";
	}
}