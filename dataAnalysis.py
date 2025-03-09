import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Load + read + clean CSV
file_path = 'experiment_results.csv'  
df = pd.read_csv(file_path)
df.columns = df.columns.str.strip()

# Calculate average time and confidence for each level
# This allows for multiple trials to be combined, but I think it makes most sense to do within-subject analysis here
level_avg = df.groupby('Level').agg(
    avg_time=('Average Time (ms)', 'mean'),
    avg_confidence=('Average Confidence', 'mean')
).reset_index()

print("\nAverage Time and Confidence by Level:")
print(level_avg)

# Plotting
sns.set(style="whitegrid")

# Plot Average Time per Level
plt.figure(figsize=(10, 5))
sns.barplot(x='Level', y='avg_time', data=level_avg, palette='Blues')
plt.title('Average Decision Time by Assistance Level')
plt.xlabel('Assistance Level')
plt.ylabel('Average Time (ms)')
plt.xticks(rotation=0)
plt.show()

# Plot Average Confidence per Level
plt.figure(figsize=(10, 5))
sns.barplot(x='Level', y='avg_confidence', data=level_avg, palette='Greens')
plt.title('Average Confidence Score by Assistance Level')
plt.xlabel('Assistance Level')
plt.ylabel('Average Confidence (/10)')
plt.xticks(rotation=0)
plt.show()

# Exports processed data - just to make sure
level_avg.to_csv('processed_results.csv', index=False)
print("Processed data saved to 'processed_results.csv'")
