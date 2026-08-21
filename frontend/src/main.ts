import { bootstrapApplication } from '@angular/platform-browser';
import { Component, inject } from '@angular/core';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FormsModule],
  template: `
    <main class="shell">
      <header>
        <p class="eyebrow">PRIVATE LOCAL AI</p>
        <h1>Language Learning Engine</h1>
        <p class="lede">Grounded practice from your own study materials.</p>
      </header>
      <section class="panel">
        <div class="panel-head">
          <div>
            <p class="eyebrow">MODEL PLAYGROUND</p>
            <h2>Ask the local model</h2>
          </div>
          <span [class.ready]="models.length > 0" class="status">{{ models.length ? 'CONNECTED' : 'CHECKING' }}</span>
        </div>
        <label for="model">Model</label>
        <select id="model" [(ngModel)]="model">
          @for (availableModel of models; track availableModel) {
            <option [value]="availableModel">{{ availableModel }}</option>
          }
        </select>
        <label for="prompt">Prompt</label>
        <textarea id="prompt" [(ngModel)]="prompt" rows="5"></textarea>
        <button (click)="generate()" [disabled]="loading || !prompt.trim()">
          {{ loading ? 'Generating...' : 'Generate response' }}
        </button>
        @if (response) {
          <div class="response"><p class="eyebrow">RESPONSE</p><p>{{ response }}</p></div>
        }
        @if (error) { <p class="error">{{ error }}</p> }
      </section>
    </main>
  `,
  styles: []
})
class AppComponent {
  private readonly http = inject(HttpClient);
  models: string[] = [];
  model = 'llama3.2:3b';
  prompt = 'Create one short vocabulary exercise about greetings.';
  response = '';
  error = '';
  loading = false;

  constructor() {
    void this.loadModels();
  }

  private async loadModels(): Promise<void> {
    try {
      const result = await firstValueFrom(this.http.get<{ models: string[] }>('/api/models'));
      this.models = result.models;
      if (this.models.length && !this.models.includes(this.model)) this.model = this.models[0];
    } catch {
      this.error = 'Could not connect to the local Ollama service.';
    }
  }

  async generate(): Promise<void> {
    this.loading = true;
    this.error = '';
    this.response = '';
    try {
      const result = await firstValueFrom(this.http.post<{ response: string }>('/api/generate', {
        model: this.model, prompt: this.prompt, maxTokens: 256
      }));
      this.response = result.response;
    } catch {
      this.error = 'Generation failed. Check that Ollama is running and the model is installed.';
    } finally {
      this.loading = false;
    }
  }
}

bootstrapApplication(AppComponent, { providers: [provideHttpClient()] });
