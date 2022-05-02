from sentence_transformers import SentenceTransformer
from sklearn.cluster import KMeans

embedder = SentenceTransformer('all-MiniLM-L6-v2')

corpus = [
    'Novavax Surges As Its Covid Shot Approaches The FDA Finish Line',
    'Shareholder Proposals to Broaden Access to Covid-19 Vaccines Rejected',
    'Is the Covid-19 Vaccine Safe for Kids Ages 5 and Younger?',
    'Moderna Asks FDA to Clear Its Covid Vaccine for Kids Under 6',
    'Is Moderna Stock A Buy After FDA Signs Off On Another Covid Booster?',
    'Covid-19 Vaccines Carry Low Risk of Heart Conditions, Studies Find',
    'Covid Proved Their Tech, But Moderna, BioNTech Face A New Battle'
]

corpus_embeddings = embedder.encode(corpus)

num_clusters = 2
clustering_model = KMeans(n_clusters=num_clusters)
clustering_model.fit(corpus_embeddings)

cluster_assignment = clustering_model.labels_

print(cluster_assignment)
