import { Routes } from '@angular/router';
import { ScanComponent } from './components/scan/scan.component';
import { ThreatComponent } from './components/threat/threat.component';
import { MonitorComponent } from './components/monitor/monitor.component';

export const routes: Routes = [
  { path: 'scan', component: ScanComponent },
  { path: 'threats', component: ThreatComponent },
  { path: 'monitor', component: MonitorComponent }
];
  