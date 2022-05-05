from transformers import AutoTokenizer, AutoModelForSequenceClassification, AutoModelForQuestionAnswering

cross_encoder_model_name = 'cross-encoder/ms-marco-MiniLM-L-6-v2'
question_answer_model_name = 'bert-large-uncased-whole-word-masking-finetuned-squad'
zero_shot_classification_model_name = 'facebook/bart-large-mnli'
classifier_model_name = 'sentence-transformers/all-MiniLM-L6-v2'


def save_pretrained_seq_classification(model_name: str):
    """
    Downloads a pretrained model and save them to local filesystem on the project path
    so they do not have to be re-downloaded, furthermore they can be added to Docker containers or 
    VM runtimes for a hosted API solution
    :param model_name: the HuggingFace pretrained model name
    :return: nothing
    """
    print(f"Fetching pre-trained model {model_name}")
    tokenizer = AutoTokenizer.from_pretrained(model_name)
    model = AutoModelForSequenceClassification.from_pretrained(model_name)
    print(f"Saving pre-trained model {model_name} to local ./{model_name}")
    model.save_pretrained(f'./{model_name}')
    tokenizer.save_pretrained(f'./{model_name}')


def save_pretrained_question_answering(model_name: str):
    """
    Downloads a pretrained model and save them to local filesystem on the project path
    so they do not have to be re-downloaded, furthermore they can be added to Docker containers or 
    VM runtimes for a hosted API solution
    :param model_name: the HuggingFace pretrained model name
    :return: nothing
    """
    print(f"Fetching pre-trained model {model_name}")
    tokenizer = AutoTokenizer.from_pretrained(model_name)
    model = AutoModelForQuestionAnswering.from_pretrained(model_name)
    print(f"Saving pre-trained model {model_name} to local ./{model_name}")
    model.save_pretrained(f'./{model_name}')
    tokenizer.save_pretrained(f'./{model_name}')


if __name__ == "__main__":
    save_pretrained_seq_classification(cross_encoder_model_name)
    save_pretrained_seq_classification(classifier_model_name)
    save_pretrained_seq_classification(zero_shot_classification_model_name)
    save_pretrained_question_answering(question_answer_model_name)
