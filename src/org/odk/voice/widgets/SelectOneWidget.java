package org.odk.voice.widgets;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.OrderedHashtable;
import org.odk.voice.constants.StringConstants;
import org.odk.voice.vxml.VxmlDocument;
import org.odk.voice.vxml.VxmlForm;
import org.odk.voice.vxml.VxmlUtils;
import org.odk.voice.xform.PromptElement;

public class SelectOneWidget extends QuestionWidget {
  
  public SelectOneWidget(PromptElement p) {
    super(p);
  }
  
//  @Override
//  public String[] getPromptStrings() {
//    List<String> ps = new ArrayList<String>();
//    ps.add(StringConstants.questionXOfY(questionNum, totalNum));
//    ps.add(StringConstants.select1Instructions);
//    if (prompt.getSelectItems()!=null) {
//      OrderedHashtable h = prompt.getSelectItems();
//      Enumeration items = h.keys();
//      int i = 1;
//      while (items.hasMoreElements()) {
//          ps.add(StringConstants.select1Press(i));
//          ps.add((String) items.nextElement());
//          i++;
//      }
//    }
//    ps.add(StringConstants.answerConfirmationKeypad);
//    ps.add(StringConstants.answerConfirmationOptions);
//    ps.add(StringConstants.thankYou);
//    return ps.toArray(new String[]{});
//  }
  
  public void addConfAudio(String textAndAudio, StringBuilder confPrompt, List<String> confPromptStrings){
    confPrompt.append(VxmlUtils.getAudio(textAndAudio));
    confPromptStrings.add(textAndAudio);
  }
  public void getPromptVxml(Writer out) throws IOException{
    List<String> promptSegments = new ArrayList<String>();
    List<String> grammarKeys = new ArrayList<String>();
    List<String> grammarTags = new ArrayList<String>();
    promptSegments.add(prompt.getQuestionText());
    promptSegments.add(StringConstants.select1Instructions);
    
    StringBuilder confPrompt = new StringBuilder("<prompt>\n");
    List<String> confPromptStrings = new ArrayList<String>();
    
    addConfAudio(StringConstants.answerConfirmationKeypad, confPrompt, confPromptStrings);
    
    if (prompt.getSelectItems() != null) {
      OrderedHashtable h = prompt.getSelectItems();
      Enumeration items = h.keys();
      String itemLabel = null;
      String itemValue = null;
      
      int i = 1;

      while (items.hasMoreElements()) {
          itemLabel = (String) items.nextElement();
          itemValue = (String) h.get(itemLabel);
          promptSegments.add(StringConstants.select1Press(i));
          promptSegments.add(itemLabel);
          grammarKeys.add(Integer.toString(i));
          grammarTags.add("out.label=\"" + itemLabel + "\"; out.answer=\"" + itemValue + "\";");
          
          confPrompt.append("<" + (i==1?"if":"elseif") + " expr=\"answer=='" + itemValue + "'\"" + (i==1?"":"/") + ">\n");
          addConfAudio(itemLabel, confPrompt, confPromptStrings);
          
          i++;
      }
      confPrompt.append("</if>\n");
      addConfAudio(StringConstants.answerConfirmationOptions, confPrompt, confPromptStrings);
      confPrompt.append("</prompt>\n");
      
      VxmlForm answerForm = new VxmlForm("answer", 
          createPrompt(promptSegments.toArray(new String[]{})), 
          VxmlUtils.createGrammar(grammarKeys.toArray(new String[]{}), grammarTags.toArray(new String[]{})),
          VxmlUtils.createGoto("#confirm")
          );
      
      VxmlForm confirmForm = new VxmlForm("confirm", 
          createBasicPrompt(confPrompt.toString(), confPromptStrings.toArray(new String[]{})), 
          VxmlUtils.confirmGrammar,
          VxmlUtils.confirmFilled(this));
      
      VxmlDocument d = new VxmlDocument(questionCountForm, answerForm, confirmForm);
      d.write(out);
    }
  }
    
  @Override
  public IAnswerData getAnswer(String stringData, InputStream binaryData)
      throws IllegalArgumentException {
    if (stringData == null)
      return null;
    return new SelectOneData(new Selection(stringData));
  }

}