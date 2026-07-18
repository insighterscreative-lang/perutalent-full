import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-logo',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './logo.html',
  styleUrl: './logo.scss'
})
export class LogoComponent {

  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() showText: boolean = true;
} 