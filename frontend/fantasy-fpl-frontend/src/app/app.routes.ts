import { Routes } from '@angular/router';
import { MatchesComponent } from './components/matches/matches.component';
import { TableComponent } from './components/table/table.component';
import { InjuriesComponent } from './components/injuries/injuries.component';
import { ClubsComponent } from './components/clubs/clubs.component';
import { ClubDetailComponent } from './components/club-detail/club-detail.component';
import { StatisticsComponent } from './components/statistics/statistics.component';
import { HomeComponent } from './components/home/home.component'; // Import Home

export const routes: Routes = [
  // CHANGE THIS LINE: Redirect '' to 'home' (or component: HomeComponent directly)
  { path: '', component: HomeComponent }, 
  
  { path: 'matches', component: MatchesComponent },
  { path: 'table', component: TableComponent },
  { path: 'injuries', component: InjuriesComponent },
  { path: 'clubs', component: ClubsComponent },
  { path: 'clubs/:id', component: ClubDetailComponent },
  { path: 'statistics', component: StatisticsComponent }
];